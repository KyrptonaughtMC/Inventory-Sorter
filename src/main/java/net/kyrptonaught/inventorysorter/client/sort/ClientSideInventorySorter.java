package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.sort.plan.ClientFallbackSortPlanBuilder;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.client.Minecraft;

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
    private final Supplier<List<SortPriorityRuleSetting>> sortPriorityRules;
    private final BooleanSupplier sortPlayerInventory;
    private final BooleanSupplier sortIntoBundles;
    private final BooleanSupplier sortIntoHotbarBundles;
    private final ClientInventoryClickExecutor clickExecutor;
    private final ClientFallbackSortPlanBuilder sortPlanBuilder;

    ClientSideInventorySorter(
            Supplier<Minecraft> minecraft,
            Supplier<String> languageCode,
            Supplier<SortType> sortType,
            Supplier<List<SortPriorityRuleSetting>> sortPriorityRules,
            BooleanSupplier sortPlayerInventory,
            BooleanSupplier sortIntoBundles,
            BooleanSupplier sortIntoHotbarBundles,
            ClientInventoryClickExecutor clickExecutor,
            ClientFallbackSortPlanBuilder sortPlanBuilder
    ) {
        this.minecraft = minecraft;
        this.languageCode = languageCode;
        this.sortType = sortType;
        this.sortPriorityRules = sortPriorityRules;
        this.sortPlayerInventory = sortPlayerInventory;
        this.sortIntoBundles = sortIntoBundles;
        this.sortIntoHotbarBundles = sortIntoHotbarBundles;
        this.clickExecutor = clickExecutor;
        this.sortPlanBuilder = sortPlanBuilder;
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

        ClientSortScope sortScope = scope.get();
        Optional<List<PlannedContainerClick>> plannedClicks = sortPlanBuilder.build(
                sortScope,
                sortType.get(),
                languageCode.get(),
                sortPriorityRules.get(),
                sortIntoBundles.getAsBoolean(),
                sortIntoHotbarBundles.getAsBoolean()
        );
        if (plannedClicks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ClientInventoryClickExecutor.QueuedSort(sortScope.menuId(), plannedClicks.get()));
    }
}
