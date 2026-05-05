package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.sort.ordering.StackOrderingStrategy;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SortedInventoryLayout(List<ItemStack> stacks) {
    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode) {
        return from(input, sortType, languageCode, List.of());
    }

    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode, List<SortPriorityRule> sortPriorityRules) {
        SortPriorityRules priorityRules = SortPriorityRules.compile(sortPriorityRules);
        List<ItemStack> mergedStacks = new ArrayList<>();
        List<ItemStack> sortedStacks = new ArrayList<>(input.size());
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.get(i);
            if (priorityRules.shouldIgnore(stack)) {
                sortedStacks.add(stack.copy());
            } else {
                sortedStacks.add(ItemStack.EMPTY);
                addStackWithMerge(mergedStacks, stack.copy());
            }
        }

        StackOrderingStrategy orderingStrategy = StackOrderingStrategy.bySortType(sortType, languageCode);
        mergedStacks.sort(priorityRules.applyTo(orderingStrategy.comparator()));

        int sortedIndex = 0;
        for (int i = 0; i < sortedStacks.size(); i++) {
            if (!sortedStacks.get(i).isEmpty()) {
                continue;
            }
            sortedStacks.set(i, sortedIndex < mergedStacks.size() ? mergedStacks.get(sortedIndex) : ItemStack.EMPTY);
            sortedIndex++;
        }
        return new SortedInventoryLayout(List.copyOf(sortedStacks));
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.isEmpty()) {
            return;
        }
        if (newStack.isStackable() && newStack.getCount() != newStack.getMaxStackSize()) {
            for (int j = stacks.size() - 1; j >= 0; j--) {
                ItemStack oldStack = stacks.get(j);
                if (SortableItemStackRules.canMerge(oldStack, newStack)) {
                    SortableItemStackRules.mergeInto(oldStack, newStack);
                    if (oldStack.isEmpty()) {
                        stacks.remove(j);
                    }
                }
            }
        }
        stacks.add(newStack);
    }
}
