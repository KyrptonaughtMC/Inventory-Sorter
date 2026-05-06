package net.kyrptonaught.inventorysorter.client.sort.plan;

final class SwapThroughCursorStep implements SlotPlanningStep {
    @Override
    public boolean plan(ClickPlanningState state, int target) {
        int origin = state.findMatchingSlot(state.desiredStack(target), target + 1);
        if (origin == -1) {
            return false;
        }

        state.swapThroughCursor(target, origin);
        return true;
    }
}
