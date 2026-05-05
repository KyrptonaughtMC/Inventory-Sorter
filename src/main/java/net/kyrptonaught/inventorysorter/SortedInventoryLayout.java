package net.kyrptonaught.inventorysorter;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SortedInventoryLayout(List<ItemStack> stacks) {
    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode) {
        return from(input, sortType, languageCode, List.of());
    }

    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode, List<SortPriorityRule> sortPriorityRules) {
        List<ItemStack> mergedStacks = new ArrayList<>();
        for (ItemStack stack : input) {
            addStackWithMerge(mergedStacks, stack.copy());
        }

        mergedStacks.sort(SortPriorityRules.compile(sortPriorityRules).applyTo(SortCases.getComparator(sortType, languageCode)));

        List<ItemStack> sortedStacks = new ArrayList<>(input.size());
        for (int i = 0; i < input.size(); i++) {
            sortedStacks.add(i < mergedStacks.size() ? mergedStacks.get(i) : ItemStack.EMPTY);
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
