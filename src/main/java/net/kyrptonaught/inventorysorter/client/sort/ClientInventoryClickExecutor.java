package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.kyrptonaught.inventorysorter.compat.CompatibilityPlugins;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.BooleanSupplier;

class ClientInventoryClickExecutor {
    private final Queue<PlannedSort> pendingSorts = new ArrayDeque<>();
    private final BooleanSupplier allowPlayerInventorySorting;

    ClientInventoryClickExecutor() {
        this(() -> true);
    }

    ClientInventoryClickExecutor(BooleanSupplier allowPlayerInventorySorting) {
        this.allowPlayerInventorySorting = allowPlayerInventorySorting;
    }

    /**
     * Replaces pending work with the menu-bound click plans for the latest request.
     *
     * Empty plans are ignored. Queued plans are only valid while the player remains in the same
     * menu id; a later tick clears all pending work if that menu no longer matches.
     */
    public void replacePendingSorts(List<QueuedSort> sorts) {
        pendingSorts.clear();
        for (QueuedSort sort : sorts) {
            if (!sort.clicks().isEmpty()) {
                pendingSorts.add(new PlannedSort(sort.menuId(), new ArrayDeque<>(sort.clicks()), sort.target()));
            }
        }
    }

    /**
     * Drops all pending client-side sort work.
     *
     * This is used when the menu lifecycle changes or when the client session is reset. Any clicks
     * already sent before this call are not rolled back.
     */
    public void clear() {
        pendingSorts.clear();
    }

    /**
     * Sends queued clicks for the current client menu.
     *
     * The executor drains all valid queued plans in one client tick. If the player, game mode, or
     * menu id no longer matches the queued plan, the queue is cleared and no further clicks are sent
     * for that tick.
     */
    public void tick(Minecraft minecraft) {
        tick(new MinecraftClickSender(minecraft));
    }

    boolean tick(ClickSender clickSender) {
        int sentClickCount = 0;
        PlannedSort plannedSort = pendingSorts.peek();
        while (plannedSort != null) {
            if (!SortabilityPolicy.isTargetAllowed(plannedSort.target(), allowPlayerInventorySorting.getAsBoolean())) {
                pendingSorts.poll();
                plannedSort = pendingSorts.peek();
                continue;
            }
            if (!clickSender.canSend(plannedSort.menuId())) {
                InventorySorterMod.LOGGER.debug("Aborted client-side sort before sending clicks because the menu changed");
                clear();
                return sentClickCount > 0;
            }

            while (!plannedSort.clicks().isEmpty()) {
                if (!clickSender.canSend(plannedSort.menuId())) {
                    InventorySorterMod.LOGGER.debug("Aborted client-side sort after sending {} clicks because the menu changed", sentClickCount);
                    clear();
                    return sentClickCount > 0;
                }

                clickSender.send(plannedSort.clicks().poll());
                sentClickCount++;
            }

            pendingSorts.poll();
            plannedSort = pendingSorts.peek();
        }
        return sentClickCount > 0;
    }

    int pendingSortCount() {
        return pendingSorts.size();
    }

    interface ClickSender {
        boolean canSend(int menuId);

        void send(PlannedContainerClick click);
    }

    record QueuedSort(int menuId, List<PlannedContainerClick> clicks, SortTarget target) {
        QueuedSort(int menuId, List<PlannedContainerClick> clicks) {
            this(menuId, clicks, SortTarget.CONTAINER);
        }
    }

    private record PlannedSort(int menuId, Queue<PlannedContainerClick> clicks, SortTarget target) {
    }

    private record MinecraftClickSender(Minecraft minecraft) implements ClickSender {
        @Override
        public boolean canSend(int menuId) {
            return minecraft != null
                    && minecraft.player != null
                    && minecraft.gameMode != null
                    && minecraft.player.containerMenu != null
                    && minecraft.player.containerMenu.containerId == menuId;
        }

        @Override
        public void send(PlannedContainerClick click) {
            if (click.slotIndex() >= 0 && click.slotIndex() < minecraft.player.containerMenu.slots.size()) {
                Slot slot = minecraft.player.containerMenu.slots.get(click.slotIndex());
                CompatibilityPlugins.prepareClientBundleSlotClick(slot);
            }
            minecraft.gameMode.handleContainerInput(
                    minecraft.player.containerMenu.containerId,
                    click.slotIndex(),
                    click.button(),
                    click.input(),
                    minecraft.player
            );
        }
    }
}
