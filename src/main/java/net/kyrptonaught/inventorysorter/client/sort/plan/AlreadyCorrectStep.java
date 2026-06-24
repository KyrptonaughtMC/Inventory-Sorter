package net.kyrptonaught.inventorysorter.client.sort.plan;

final class AlreadyCorrectStep implements SlotPlanningStep {
    @Override
    public boolean plan(ClickPlanningState state, int target) {
        return state.targetAlreadyMatches(target);
    }
}
