package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.client.sort.ClientSortScope;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientFallbackSortPlanBuilder {
    private final ClientSortClickPlanner clickPlanner;
    private final BundleInsertionClickAdapter bundleInsertionClickAdapter;

    public ClientFallbackSortPlanBuilder(ClientSortClickPlanner clickPlanner) {
        this(clickPlanner, new BundleInsertionClickAdapter());
    }

    ClientFallbackSortPlanBuilder(
            ClientSortClickPlanner clickPlanner,
            BundleInsertionClickAdapter bundleInsertionClickAdapter
    ) {
        this.clickPlanner = clickPlanner;
        this.bundleInsertionClickAdapter = bundleInsertionClickAdapter;
    }

    public Optional<List<PlannedContainerClick>> build(
            ClientSortScope sortScope,
            SortType sortType,
            String languageCode,
            List<SortPriorityRuleSetting> rules,
            boolean sortIntoBundles,
            boolean sortIntoHotbarBundles
    ) {
        List<ItemStack> currentStacks = stacks(sortScope.slots());

        List<PlannedContainerClick> bundleInsertionClicks = List.of();
        List<ItemStack> layoutInput = currentStacks;
        if (sortIntoBundles) {
            List<ClientSortScope.ScopedSlot> extraBundleTargetSlots = sortScope.extraBundleTargetSlots(sortIntoHotbarBundles);
            BundleInsertionLayoutPass.Result bundleInsertion = BundleInsertionLayoutPass.apply(
                    currentStacks,
                    stacks(extraBundleTargetSlots),
                    SortPriorityRules.compile(rules)
            );
            bundleInsertionClicks = bundleInsertionClickAdapter.clicks(sortScope, extraBundleTargetSlots, bundleInsertion);
            layoutInput = bundleInsertion.layoutStacks();
        }

        List<ItemStack> desiredStacks = sortIntoBundles
                ? SortedInventoryLayout.fromBundleAdjusted(layoutInput, sortType, languageCode, rules).stacks()
                : SortedInventoryLayout.from(currentStacks, sortType, languageCode, rules).stacks();

        Optional<List<PlannedContainerClick>> layoutClicks = clickPlanner.plan(
                slotStates(sortScope.slots(), layoutInput),
                desiredStacks
        );
        if (layoutClicks.isEmpty()) {
            return Optional.empty();
        }

        List<PlannedContainerClick> plannedClicks = new ArrayList<>(bundleInsertionClicks);
        plannedClicks.addAll(layoutClicks.get());
        return Optional.of(plannedClicks);
    }

    private static List<SlotState> slotStates(List<ClientSortScope.ScopedSlot> slots, List<ItemStack> stacks) {
        List<SlotState> slotStates = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            ClientSortScope.ScopedSlot scopedSlot = slots.get(i);
            slotStates.add(new SlotState(
                    scopedSlot.menuSlotIndex(),
                    stacks.get(i).copy()
            ));
        }
        return slotStates;
    }

    private static List<ItemStack> stacks(List<ClientSortScope.ScopedSlot> slots) {
        return slots.stream()
                .map(scopedSlot -> scopedSlot.slot().getItem())
                .map(ItemStack::copy)
                .toList();
    }
}
