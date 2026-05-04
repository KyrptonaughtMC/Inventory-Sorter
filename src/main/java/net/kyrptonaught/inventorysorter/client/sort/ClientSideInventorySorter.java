package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.SortType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

class ClientSideInventorySorter {
    private final Supplier<Minecraft> minecraft;
    private final Supplier<String> languageCode;
    private final Supplier<SortType> sortType;
    private final BooleanSupplier sortPlayerInventory;
    private final ClientInventoryClickExecutor clickExecutor;
    private final ClientSortClickPlanner clickPlanner;

    ClientSideInventorySorter(
            Supplier<Minecraft> minecraft,
            Supplier<String> languageCode,
            Supplier<SortType> sortType,
            BooleanSupplier sortPlayerInventory,
            ClientInventoryClickExecutor clickExecutor,
            ClientSortClickPlanner clickPlanner
    ) {
        this.minecraft = minecraft;
        this.languageCode = languageCode;
        this.sortType = sortType;
        this.sortPlayerInventory = sortPlayerInventory;
        this.clickExecutor = clickExecutor;
        this.clickPlanner = clickPlanner;
    }

    /**
     * Plans a local sort for the current menu and enqueues the clicks needed to realize it.
     *
     * This does not mutate the inventory immediately. It queues vanilla container clicks that are
     * sent by {@link ClientInventoryClickExecutor} on later client ticks. If the configured
     * container-sort behavior also sorts the player inventory, this may enqueue a second plan after
     * the container plan is accepted.
     */
    public boolean enqueueCurrentScreenSort(SortTarget target) {
        Minecraft currentMinecraft = minecraft.get();
        return enqueueSortPlans(
                target,
                sortPlayerInventory,
                requestedTarget -> plan(currentMinecraft, requestedTarget),
                clickExecutor
        );
    }

    static boolean enqueueSortPlans(
            SortTarget target,
            BooleanSupplier sortPlayerInventory,
            Function<SortTarget, Optional<ClientInventoryClickExecutor.QueuedSort>> planner,
            ClientInventoryClickExecutor clickExecutor
    ) {
        Optional<ClientInventoryClickExecutor.QueuedSort> sortPlan = planner.apply(target);
        if (sortPlan.isEmpty()) {
            return false;
        }

        List<ClientInventoryClickExecutor.QueuedSort> sortPlans = new ArrayList<>();
        sortPlans.add(sortPlan.get());
        if (target == SortTarget.CONTAINER && sortPlayerInventory.getAsBoolean()) {
            planner.apply(SortTarget.PLAYER_INVENTORY).ifPresent(sortPlans::add);
        }

        clickExecutor.replacePendingSorts(sortPlans);
        return true;
    }

    private Optional<ClientInventoryClickExecutor.QueuedSort> plan(Minecraft minecraft, SortTarget target) {
        Optional<ClientSortScope> scope = ClientSortScope.resolve(minecraft, target);
        if (scope.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> currentStacks = scope.get().slots().stream()
                .map(scopedSlot -> scopedSlot.slot().getItem())
                .map(ItemStack::copy)
                .toList();
        List<ItemStack> desiredStacks = SortedInventoryLayout.from(
                currentStacks,
                sortType.get(),
                languageCode.get()
        ).stacks();

        Optional<List<ClientSortClickPlanner.PlannedContainerClick>> clicks = clickPlanner.plan(
                slotStates(scope.get().slots()),
                desiredStacks
        );
        if (clicks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ClientInventoryClickExecutor.QueuedSort(scope.get().menuId(), clicks.get()));
    }

    private static List<ClientSortClickPlanner.SlotState> slotStates(List<ClientSortScope.ScopedSlot> slots) {
        List<ClientSortClickPlanner.SlotState> slotStates = new ArrayList<>();
        for (ClientSortScope.ScopedSlot scopedSlot : slots) {
            slotStates.add(new ClientSortClickPlanner.SlotState(
                    scopedSlot.menuSlotIndex(),
                    scopedSlot.slot().getItem().copy()
            ));
        }
        return slotStates;
    }
}
