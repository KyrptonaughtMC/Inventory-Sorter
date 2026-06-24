package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
import net.minecraft.world.item.ItemStack;

final class MergePlanningPass {
    boolean plan(ClickPlanningState state) {
        boolean[] reservedExactSlots = new boolean[state.size()];
        for (int target = 0; target < state.size(); target++) {
            if (!planTarget(state, target, reservedExactSlots)) {
                return false;
            }
        }
        return true;
    }

    private boolean planTarget(ClickPlanningState state, int target, boolean[] reservedExactSlots) {
        ItemStack desiredStack = state.desiredStack(target);
        if (desiredStack.isEmpty()) {
            return true;
        }

        if (state.targetAlreadyMatches(target)) {
            reservedExactSlots[target] = true;
            return true;
        }

        int exactOrigin = state.findMatchingSlot(desiredStack, target + 1, reservedExactSlots);
        if (exactOrigin != -1) {
            reservedExactSlots[exactOrigin] = true;
            return true;
        }

        if (state.simulatedStack(target).isEmpty()) {
            int origin = state.findMergeOrigin(desiredStack, target, reservedExactSlots);
            if (origin == -1) {
                return false;
            }
            state.moveStack(origin, target);
        }

        while (needsMoreOfDesiredStack(state, desiredStack, target)) {
            int origin = state.findMergeOrigin(desiredStack, target, reservedExactSlots);
            if (origin == -1) {
                return false;
            }
            state.mergeStack(origin, target);
        }
        return true;
    }

    private boolean needsMoreOfDesiredStack(ClickPlanningState state, ItemStack desiredStack, int target) {
        ItemStack simulatedTarget = state.simulatedStack(target);
        return SortableItemStackRules.canMergeToward(simulatedTarget, desiredStack)
                && simulatedTarget.getCount() < desiredStack.getCount();
    }
}
