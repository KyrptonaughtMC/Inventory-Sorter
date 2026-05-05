package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

class ClientInventoryClickExecutor {
    private final Queue<PlannedSort> pendingSorts = new ArrayDeque<>();

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
                pendingSorts.add(new PlannedSort(sort.menuId(), new ArrayDeque<>(sort.clicks())));
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

    record QueuedSort(int menuId, List<PlannedContainerClick> clicks) {
    }

    private record PlannedSort(int menuId, Queue<PlannedContainerClick> clicks) {
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
