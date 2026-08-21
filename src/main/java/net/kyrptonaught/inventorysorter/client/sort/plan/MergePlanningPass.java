package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
import net.minecraft.world.item.ItemStack;

final class MergePlanningPass {
    boolean plan(ClickPlanningState state) {
        boolean[] reservedExactSlots = new boolean[state.size()];
        boolean[] preparedTargets = new boolean[state.size()];

        reserveExistingExactStacks(state, reservedExactSlots, preparedTargets);

        for (int target = 0; target < state.size(); target++) {
            if (!prepareMergedStack(state, target, reservedExactSlots, preparedTargets)) {
                return false;
            }
        }
        return true;
    }

    private void reserveExistingExactStacks(
            ClickPlanningState state,
            boolean[] reservedExactSlots,
            boolean[] preparedTargets
    ) {
        for (int target = 0; target < state.size(); target++) {
            ItemStack desiredStack = state.desiredStack(target);
            if (desiredStack.isEmpty()) {
                continue;
            }

            int exactSlot = state.findMatchingSlot(desiredStack, 0, reservedExactSlots);
            if (exactSlot != -1) {
                reservedExactSlots[exactSlot] = true;
                preparedTargets[target] = true;
            }
        }
    }

    private boolean prepareMergedStack(
            ClickPlanningState state,
            int target,
            boolean[] reservedExactSlots,
            boolean[] preparedTargets
    ) {
        ItemStack desiredStack = state.desiredStack(target);
        if (desiredStack.isEmpty() || preparedTargets[target]) {
            return true;
        }

        int accumulator = state.findAvailableMergeStack(desiredStack, -1, reservedExactSlots);
        if (accumulator == -1 || state.simulatedStack(accumulator).getCount() > desiredStack.getCount()) {
            return false;
        }

        while (needsMoreOfDesiredStack(state, desiredStack, accumulator)) {
            int origin = state.findAvailableMergeStack(desiredStack, accumulator, reservedExactSlots);
            if (origin == -1) {
                return false;
            }
            state.mergeStack(origin, accumulator);
        }

        boolean prepared = state.stackMatches(accumulator, desiredStack);
        if (prepared) {
            reservedExactSlots[accumulator] = true;
        }
        return prepared;
    }

    private boolean needsMoreOfDesiredStack(ClickPlanningState state, ItemStack desiredStack, int target) {
        ItemStack simulatedTarget = state.simulatedStack(target);
        return SortableItemStackRules.canMergeToward(simulatedTarget, desiredStack)
                && simulatedTarget.getCount() < desiredStack.getCount();
    }
}
