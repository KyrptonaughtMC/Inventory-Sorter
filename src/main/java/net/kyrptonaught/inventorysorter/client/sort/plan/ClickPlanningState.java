package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class ClickPlanningState {
    private final List<SlotState> slots;
    private final List<ItemStack> desired;
    private final List<ItemStack> simulated;
    private final List<PlannedContainerClick> clicks = new ArrayList<>();

    ClickPlanningState(List<SlotState> slots, List<ItemStack> desired) {
        this.slots = slots;
        this.desired = desired;
        this.simulated = slots.stream()
                .map(slot -> slot.stack().copy())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    boolean sizeMatches() {
        return slots.size() == desired.size();
    }

    int size() {
        return desired.size();
    }

    ItemStack desiredStack(int slot) {
        return desired.get(slot);
    }

    ItemStack simulatedStack(int slot) {
        return simulated.get(slot);
    }

    boolean targetAlreadyMatches(int target) {
        return SortableItemStackRules.sameLayoutStack(simulatedStack(target), desiredStack(target));
    }

    boolean hasEquivalentStacks() {
        boolean[] matched = new boolean[desired.size()];
        for (ItemStack stack : simulated) {
            int match = findUnmatched(stack, matched);
            if (match == -1) {
                return false;
            }
            matched[match] = true;
        }
        return true;
    }

    int findMatchingSlot(ItemStack desiredStack, int start) {
        for (int i = start; i < simulated.size(); i++) {
            if (SortableItemStackRules.sameLayoutStack(simulated.get(i), desiredStack)) {
                return i;
            }
        }
        return -1;
    }

    int findMatchingSlot(ItemStack desiredStack, int start, boolean[] reservedSlots) {
        for (int i = start; i < simulated.size(); i++) {
            if (!reservedSlots[i] && SortableItemStackRules.sameLayoutStack(simulated.get(i), desiredStack)) {
                return i;
            }
        }
        return -1;
    }

    int findEmptySlot(int start) {
        for (int i = start; i < simulated.size(); i++) {
            if (simulated.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    int findMergeOrigin(ItemStack desiredStack, int target) {
        for (int i = 0; i < simulated.size(); i++) {
            if (canUseAsMergeOrigin(i, target, desiredStack)) {
                return i;
            }
        }
        return -1;
    }

    int findMergeOrigin(ItemStack desiredStack, int target, boolean[] reservedSlots) {
        for (int i = 0; i < simulated.size(); i++) {
            if (!reservedSlots[i] && canUseAsMergeOrigin(i, target, desiredStack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean canUseAsMergeOrigin(int slot, int target, ItemStack desiredStack) {
        return slot != target
                && SortableItemStackRules.canMergeToward(simulated.get(slot), desiredStack)
                && simulated.get(slot).getCount() < simulated.get(slot).getMaxStackSize()
                && !SortableItemStackRules.sameLayoutStack(simulated.get(slot), desired.get(slot));
    }

    void moveStack(int origin, int target) {
        click(origin);
        click(target);
        simulated.set(target, simulated.get(origin));
        simulated.set(origin, ItemStack.EMPTY);
    }

    void mergeStack(int origin, int target) {
        click(origin);
        click(target);

        ItemStack carried = simulated.get(origin);
        ItemStack targetStack = simulated.get(target);
        SortableItemStackRules.mergeInto(carried, targetStack);
        simulated.set(origin, ItemStack.EMPTY);

        if (!carried.isEmpty()) {
            click(origin);
            simulated.set(origin, carried);
        }
    }

    void swapThroughCursor(int target, int origin) {
        click(target);
        click(origin);
        click(target);

        ItemStack targetStack = simulated.get(target);
        simulated.set(target, simulated.get(origin));
        simulated.set(origin, targetStack);
    }

    List<PlannedContainerClick> plannedClicks() {
        return List.copyOf(clicks);
    }

    private int findUnmatched(ItemStack stack, boolean[] matched) {
        for (int i = 0; i < desired.size(); i++) {
            if (!matched[i] && SortableItemStackRules.sameLayoutStack(stack, desired.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void click(int slot) {
        clicks.add(new PlannedContainerClick(slots.get(slot).menuSlotIndex(), 0, ContainerInput.PICKUP));
    }
}
