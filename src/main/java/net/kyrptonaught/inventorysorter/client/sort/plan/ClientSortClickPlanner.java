package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ClientSortClickPlanner {
    private final MergePlanningPass mergePlanningPass = new MergePlanningPass();
    private final List<SlotPlanningStep> slotPlanningChain = List.of(
            new AlreadyCorrectStep(),
            new DirectMoveIntoEmptyTargetStep(),
            new MoveViaEmptySlotStep(),
            new SwapThroughCursorStep()
    );

    public Optional<List<PlannedContainerClick>> plan(List<SlotState> current, List<ItemStack> desired) {
        ClickPlanningState state = new ClickPlanningState(current, desired);
        if (!state.sizeMatches()) {
            return Optional.empty();
        }

        if (!mergePlanningPass.plan(state) || !state.hasEquivalentStacks()) {
            return Optional.empty();
        }

        for (int target = 0; target < state.size(); target++) {
            if (!planTarget(state, target)) {
                return Optional.empty();
            }
        }

        return Optional.of(state.plannedClicks());
    }

    private boolean planTarget(ClickPlanningState state, int target) {
        for (SlotPlanningStep step : slotPlanningChain) {
            if (step.plan(state, target)) {
                return true;
            }
        }
        return false;
    }
}
