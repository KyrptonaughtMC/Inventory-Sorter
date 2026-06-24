package net.kyrptonaught.inventorysorter.client.sort.plan;

final class DirectMoveIntoEmptyTargetStep implements SlotPlanningStep {
    @Override
    public boolean plan(ClickPlanningState state, int target) {
        int origin = state.findMatchingSlot(state.desiredStack(target), target + 1);
        if (origin == -1 || !state.simulatedStack(target).isEmpty()) {
            return false;
        }

        state.moveStack(origin, target);
        return true;
    }
}
