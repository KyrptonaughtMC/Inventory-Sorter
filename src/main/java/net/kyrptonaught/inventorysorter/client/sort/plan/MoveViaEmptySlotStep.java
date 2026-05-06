package net.kyrptonaught.inventorysorter.client.sort.plan;

final class MoveViaEmptySlotStep implements SlotPlanningStep {
    @Override
    public boolean plan(ClickPlanningState state, int target) {
        int origin = state.findMatchingSlot(state.desiredStack(target), target + 1);
        int emptySlot = state.findEmptySlot(target + 1);
        if (origin == -1 || emptySlot == -1 || emptySlot == origin) {
            return false;
        }

        state.moveStack(target, emptySlot);
        state.moveStack(origin, target);
        return true;
    }
}
