package net.kyrptonaught.inventorysorter.sort;

import net.minecraft.world.item.ItemStack;

public final class SortableItemStackRules {
    private SortableItemStackRules() {
    }

    public static boolean sameIdentity(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && ItemStack.isSameItemSameComponents(first, second);
    }

    public static boolean sameLayoutStack(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.getCount() == second.getCount() && sameIdentity(first, second);
    }

    public static boolean canMerge(ItemStack source, ItemStack target) {
        return sameIdentity(source, target)
                && source.isStackable()
                && source.getCount() < source.getMaxStackSize()
                && target.getCount() < target.getMaxStackSize();
    }

    public static boolean canMergeToward(ItemStack source, ItemStack desiredTarget) {
        return sameIdentity(source, desiredTarget) && source.isStackable();
    }

    public static int transferableAmount(ItemStack source, ItemStack target) {
        if (!canMergeToward(source, target) || target.getCount() >= target.getMaxStackSize()) {
            return 0;
        }
        return Math.min(source.getCount(), target.getMaxStackSize() - target.getCount());
    }

    public static int mergeInto(ItemStack source, ItemStack target) {
        int moved = transferableAmount(source, target);
        target.grow(moved);
        source.shrink(moved);
        return moved;
    }
}
