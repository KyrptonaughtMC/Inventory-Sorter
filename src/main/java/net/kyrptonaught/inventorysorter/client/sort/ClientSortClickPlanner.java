package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortableItemStackRules;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientSortClickPlanner {
    public Optional<List<PlannedContainerClick>> plan(List<SlotState> current, List<ItemStack> desired) {
        if (current.size() != desired.size()) {
            return Optional.empty();
        }

        List<ItemStack> simulated = current.stream()
                .map(slot -> slot.stack().copy())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<PlannedContainerClick> clicks = new ArrayList<>();

        if (!planMerges(simulated, clicks, current, desired) || !hasEquivalentStacks(simulated, desired)) {
            return Optional.empty();
        }

        for (int target = 0; target < desired.size(); target++) {
            if (SortableItemStackRules.sameLayoutStack(simulated.get(target), desired.get(target))) {
                continue;
            }

            int origin = findMatchingSlot(simulated, desired.get(target), target + 1);
            if (origin == -1) {
                return Optional.empty();
            }

            if (simulated.get(target).isEmpty()) {
                moveStack(simulated, clicks, current, origin, target);
                continue;
            }

            int emptySlot = findEmptySlot(simulated, target + 1);
            if (emptySlot != -1 && emptySlot != origin) {
                moveStack(simulated, clicks, current, target, emptySlot);
                moveStack(simulated, clicks, current, origin, target);
                continue;
            }

            swapThroughCursor(simulated, clicks, current, target, origin);
        }

        return Optional.of(List.copyOf(clicks));
    }

    private static boolean planMerges(
            List<ItemStack> simulated,
            List<PlannedContainerClick> clicks,
            List<SlotState> slots,
            List<ItemStack> desired
    ) {
        for (int target = 0; target < desired.size(); target++) {
            ItemStack desiredStack = desired.get(target);
            if (desiredStack.isEmpty()) {
                continue;
            }

            if (simulated.get(target).isEmpty()) {
                int origin = findMergeOrigin(simulated, desired, desiredStack, target);
                if (origin == -1) {
                    return false;
                }
                moveStack(simulated, clicks, slots, origin, target);
            }

            while (SortableItemStackRules.canMergeToward(simulated.get(target), desiredStack)
                    && simulated.get(target).getCount() < desiredStack.getCount()) {
                int origin = findMergeOrigin(simulated, desired, desiredStack, target);
                if (origin == -1) {
                    return false;
                }
                mergeStack(simulated, clicks, slots, origin, target);
            }
        }
        return true;
    }

    private static int findMergeOrigin(List<ItemStack> simulated, List<ItemStack> desired, ItemStack desiredStack, int target) {
        for (int i = 0; i < simulated.size(); i++) {
            if (i != target
                    && SortableItemStackRules.canMergeToward(simulated.get(i), desiredStack)
                    && !SortableItemStackRules.sameLayoutStack(simulated.get(i), desired.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void mergeStack(
            List<ItemStack> simulated,
            List<PlannedContainerClick> clicks,
            List<SlotState> slots,
            int origin,
            int target
    ) {
        click(clicks, slots, origin);
        click(clicks, slots, target);

        ItemStack carried = simulated.get(origin);
        ItemStack targetStack = simulated.get(target);
        SortableItemStackRules.mergeInto(carried, targetStack);
        simulated.set(origin, ItemStack.EMPTY);

        if (!carried.isEmpty()) {
            click(clicks, slots, origin);
            simulated.set(origin, carried);
        }
    }

    private static boolean hasEquivalentStacks(List<ItemStack> current, List<ItemStack> desired) {
        boolean[] matched = new boolean[desired.size()];
        for (ItemStack stack : current) {
            int match = findUnmatched(stack, desired, matched);
            if (match == -1) {
                return false;
            }
            matched[match] = true;
        }
        return true;
    }

    private static int findUnmatched(ItemStack stack, List<ItemStack> desired, boolean[] matched) {
        for (int i = 0; i < desired.size(); i++) {
            if (!matched[i] && SortableItemStackRules.sameLayoutStack(stack, desired.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int findMatchingSlot(List<ItemStack> simulated, ItemStack desired, int start) {
        for (int i = start; i < simulated.size(); i++) {
            if (SortableItemStackRules.sameLayoutStack(simulated.get(i), desired)) {
                return i;
            }
        }
        return -1;
    }

    private static int findEmptySlot(List<ItemStack> simulated, int start) {
        for (int i = start; i < simulated.size(); i++) {
            if (simulated.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static void moveStack(
            List<ItemStack> simulated,
            List<PlannedContainerClick> clicks,
            List<SlotState> slots,
            int origin,
            int target
    ) {
        click(clicks, slots, origin);
        click(clicks, slots, target);
        simulated.set(target, simulated.get(origin));
        simulated.set(origin, ItemStack.EMPTY);
    }

    private static void swapThroughCursor(
            List<ItemStack> simulated,
            List<PlannedContainerClick> clicks,
            List<SlotState> slots,
            int target,
            int origin
    ) {
        click(clicks, slots, target);
        click(clicks, slots, origin);
        click(clicks, slots, target);

        ItemStack targetStack = simulated.get(target);
        simulated.set(target, simulated.get(origin));
        simulated.set(origin, targetStack);
    }

    private static void click(List<PlannedContainerClick> clicks, List<SlotState> slots, int slot) {
        clicks.add(new PlannedContainerClick(slots.get(slot).menuSlotIndex(), 0, ContainerInput.PICKUP));
    }

    public record SlotState(int menuSlotIndex, ItemStack stack) {
    }

    public record PlannedContainerClick(int slotIndex, int button, ContainerInput input) {
    }
}
