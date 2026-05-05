package net.kyrptonaught.inventorysorter.client.sort.plan;

interface SlotPlanningStep {
    boolean plan(ClickPlanningState state, int target);
}
