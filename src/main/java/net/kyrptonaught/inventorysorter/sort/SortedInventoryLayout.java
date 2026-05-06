package net.kyrptonaught.inventorysorter.sort;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.kyrptonaught.inventorysorter.sort.ordering.StackOrderingStrategy;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record SortedInventoryLayout(List<ItemStack> stacks) {
    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode) {
        return from(input, sortType, languageCode, List.of());
    }

    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode, List<SortPriorityRuleSetting> sortPriorityRules) {
        return from(input, sortType, languageCode, sortPriorityRules, false);
    }

    public static SortedInventoryLayout from(List<ItemStack> input, SortType sortType, String languageCode, List<SortPriorityRuleSetting> sortPriorityRules, boolean sortIntoBundles) {
        SortPriorityRules priorityRules = SortPriorityRules.compile(sortPriorityRules);
        List<ItemStack> bundleAdjustedInput = sortIntoBundles
                ? BundleInsertionLayoutPass.apply(input, priorityRules)
                : input;
        return fromPreparedStacks(bundleAdjustedInput, sortType, languageCode, priorityRules, sortIntoBundles);
    }

    /**
     * Builds the final layout from stacks that have already gone through bundle insertion.
     *
     * <p>Use this when integration code has to expose extra bundle targets that are not part
     * of the reordered layout, such as hotbar bundles during player main-inventory sorting.
     * The input is not passed through bundle insertion again, but bundle-aware ordering still
     * applies so top-level layout matches normal bundle sorting behavior.
     */
    public static SortedInventoryLayout fromBundleAdjusted(List<ItemStack> input, SortType sortType, String languageCode, List<SortPriorityRuleSetting> sortPriorityRules) {
        return fromPreparedStacks(input, sortType, languageCode, SortPriorityRules.compile(sortPriorityRules), true);
    }

    private static SortedInventoryLayout fromPreparedStacks(List<ItemStack> input, SortType sortType, String languageCode, SortPriorityRules priorityRules, boolean sortIntoBundles) {
        SortableInventorySnapshot snapshot = splitIgnoredStacks(input, priorityRules);
        List<ItemStack> sortedPool = sortPool(snapshot.sortablePool(), sortType, languageCode, priorityRules, sortIntoBundles);
        return new SortedInventoryLayout(fillSortableSlots(snapshot.outputShape(), sortedPool));
    }

    private static SortableInventorySnapshot splitIgnoredStacks(List<ItemStack> input, SortPriorityRules priorityRules) {
        List<ItemStack> sortablePool = new ArrayList<>();
        List<ItemStack> outputShape = new ArrayList<>(input.size());
        for (ItemStack stack : input) {
            if (priorityRules.shouldIgnore(stack)) {
                outputShape.add(stack.copy());
            } else {
                outputShape.add(ItemStack.EMPTY);
                addStackWithMerge(sortablePool, stack.copy());
            }
        }
        return new SortableInventorySnapshot(outputShape, sortablePool);
    }

    private static List<ItemStack> sortPool(List<ItemStack> sortablePool, SortType sortType, String languageCode, SortPriorityRules priorityRules, boolean sortIntoBundles) {
        List<ItemStack> sortedPool = new ArrayList<>(sortablePool);
        sortedPool.sort(ordering(sortablePool, sortType, languageCode, priorityRules, sortIntoBundles));
        return sortedPool;
    }

    private static Comparator<ItemStack> ordering(List<ItemStack> sortablePool, SortType sortType, String languageCode, SortPriorityRules priorityRules, boolean sortIntoBundles) {
        StackOrderingStrategy orderingStrategy = StackOrderingStrategy.bySortType(sortType, languageCode);
        Comparator<ItemStack> baseOrdering = orderingStrategy.comparator();
        if (sortIntoBundles) {
            baseOrdering = BundleInsertionLayoutPass.targetAwareOrdering(sortablePool, baseOrdering);
        }
        return priorityRules.applyTo(baseOrdering);
    }

    private static List<ItemStack> fillSortableSlots(List<ItemStack> outputShape, List<ItemStack> sortedPool) {
        List<ItemStack> output = new ArrayList<>(outputShape);
        int sortedIndex = 0;
        for (int i = 0; i < output.size(); i++) {
            if (!output.get(i).isEmpty()) {
                continue;
            }
            output.set(i, sortedIndex < sortedPool.size() ? sortedPool.get(sortedIndex) : ItemStack.EMPTY);
            sortedIndex++;
        }
        return List.copyOf(output);
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.isEmpty()) {
            return;
        }
        mergeIntoCompatibleStacks(stacks, newStack);
        stacks.add(newStack);
    }

    private static void mergeIntoCompatibleStacks(List<ItemStack> stacks, ItemStack newStack) {
        if (!canAbsorbIntoEarlierStacks(newStack)) {
            return;
        }
        for (int j = stacks.size() - 1; j >= 0; j--) {
            mergeIntoCompatibleStack(stacks, j, newStack);
        }
    }

    private static boolean canAbsorbIntoEarlierStacks(ItemStack stack) {
        return stack.isStackable() && stack.getCount() != stack.getMaxStackSize();
    }

    private static void mergeIntoCompatibleStack(List<ItemStack> stacks, int index, ItemStack newStack) {
        ItemStack oldStack = stacks.get(index);
        if (!SortableItemStackRules.canMerge(oldStack, newStack)) {
            return;
        }
        SortableItemStackRules.mergeInto(oldStack, newStack);
        if (oldStack.isEmpty()) {
            stacks.remove(index);
        }
    }

    private record SortableInventorySnapshot(List<ItemStack> outputShape, List<ItemStack> sortablePool) {
    }
}
