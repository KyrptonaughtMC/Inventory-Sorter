package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.client.sort.plan.ClientSortClickPlanner;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.kyrptonaught.inventorysorter.client.sort.plan.SlotState;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static net.minecraft.core.component.DataComponents.ITEM_NAME;
public class ClientSortClickPlannerTest {
    private final ClientSortClickPlanner planner = new ClientSortClickPlanner();

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void alreadySortedLayoutEmitsNoClicks() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        ItemStack apple = stack(Items.APPLE, 1);
        List<ItemStack> desired = List.of(diamond.copy(), apple.copy());

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(diamond, apple),
                desired
        );

        Assertions.assertTrue(clicks.isPresent());
        Assertions.assertTrue(clicks.get().isEmpty());
        assertClicksReachDesiredLayout(List.of(diamond, apple), clicks.get(), desired);
    }

    @Test
    void simpleTwoSlotSwapUsesPickupClicks() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        ItemStack apple = stack(Items.APPLE, 1);
        List<ItemStack> current = List.of(diamond, apple);
        List<ItemStack> desired = List.of(apple.copy(), diamond.copy());

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertEquals(List.of(
                click(0),
                click(1),
                click(0)
        ), clicks.orElseThrow());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void threeSlotCycleUsesRepeatedSwapsThroughCursor() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        ItemStack apple = stack(Items.APPLE, 1);
        ItemStack cactus = stack(Items.CACTUS, 1);
        List<ItemStack> current = List.of(diamond, apple, cactus);
        List<ItemStack> desired = List.of(cactus.copy(), diamond.copy(), apple.copy());

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertEquals(List.of(
                click(0),
                click(2),
                click(0),
                click(1),
                click(2),
                click(1)
        ), clicks.orElseThrow());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void cycleWithEmptySlotUsesTheEmptySlotAsWorkspace() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        ItemStack apple = stack(Items.APPLE, 1);
        List<ItemStack> current = List.of(ItemStack.EMPTY, diamond, apple);
        List<ItemStack> desired = List.of(apple.copy(), diamond.copy(), ItemStack.EMPTY);

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertEquals(List.of(
                click(2),
                click(0)
        ), clicks.orElseThrow());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void occupiedTargetCanMoveThroughLaterEmptySlot() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        ItemStack apple = stack(Items.APPLE, 1);
        List<ItemStack> current = List.of(diamond, apple, ItemStack.EMPTY);
        List<ItemStack> desired = List.of(apple.copy(), diamond.copy(), ItemStack.EMPTY);

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertTrue(clicks.isPresent());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void emptyDesiredTargetCanBeSwappedThroughCursor() {
        ItemStack diamond = stack(Items.DIAMOND, 1);
        List<ItemStack> current = List.of(diamond, ItemStack.EMPTY);
        List<ItemStack> desired = List.of(ItemStack.EMPTY, diamond.copy());

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertTrue(clicks.isPresent());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void partialCompatibleStacksMergeWithPickupClicks() {
        List<ItemStack> current = List.of(stack(Items.DIAMOND, 32), stack(Items.DIAMOND, 32));
        List<ItemStack> desired = List.of(stack(Items.DIAMOND, 64), ItemStack.EMPTY);

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertEquals(List.of(
                click(1),
                click(0)
        ), clicks.orElseThrow());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void mergeRemainderIsPlacedBackIntoOriginSlot() {
        List<ItemStack> current = List.of(stack(Items.DIAMOND, 33), stack(Items.DIAMOND, 32));
        List<ItemStack> desired = List.of(stack(Items.DIAMOND, 64), stack(Items.DIAMOND, 1));

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertEquals(List.of(
                click(1),
                click(0),
                click(1)
        ), clicks.orElseThrow());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void sameItemWithDifferentComponentsIsNotTreatedAsEquivalent() {
        ItemStack namedDiamond = stack(Items.DIAMOND, 1);
        namedDiamond.set(ITEM_NAME, Component.literal("Named Diamond"));
        List<ItemStack> current = List.of(stack(Items.DIAMOND, 1), namedDiamond);
        List<ItemStack> desired = List.of(namedDiamond.copy(), stack(Items.DIAMOND, 1));

        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(current),
                desired
        );

        Assertions.assertTrue(clicks.isPresent());
        Assertions.assertEquals(List.of(click(0), click(1), click(0)), clicks.get());
        assertClicksReachDesiredLayout(current, clicks.get(), desired);
    }

    @Test
    void mismatchedSlotCountsCannotBePlanned() {
        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(stack(Items.DIAMOND, 1)),
                List.of(stack(Items.DIAMOND, 1), ItemStack.EMPTY)
        );

        Assertions.assertTrue(clicks.isEmpty());
    }

    @Test
    void missingDesiredStackCannotBePlanned() {
        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(stack(Items.DIAMOND, 1), ItemStack.EMPTY),
                List.of(stack(Items.APPLE, 1), ItemStack.EMPTY)
        );

        Assertions.assertTrue(clicks.isEmpty());
    }

    @Test
    void emptyMergeTargetWithoutCompatibleOriginCannotBePlanned() {
        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(ItemStack.EMPTY, stack(Items.APPLE, 1)),
                List.of(stack(Items.DIAMOND, 64), ItemStack.EMPTY)
        );

        Assertions.assertTrue(clicks.isEmpty());
    }

    @Test
    void impossibleMergeCannotBePlanned() {
        Optional<List<PlannedContainerClick>> clicks = planner.plan(
                slots(stack(Items.DIAMOND, 32), ItemStack.EMPTY),
                List.of(stack(Items.DIAMOND, 64), ItemStack.EMPTY)
        );

        Assertions.assertTrue(clicks.isEmpty());
    }

    private static List<SlotState> slots(ItemStack... stacks) {
        return java.util.stream.IntStream.range(0, stacks.length)
                .mapToObj(i -> new SlotState(i, stacks[i]))
                .toList();
    }

    private static List<SlotState> slots(List<ItemStack> stacks) {
        return slots(stacks.toArray(ItemStack[]::new));
    }

    private static void assertClicksReachDesiredLayout(
            List<ItemStack> current,
            List<PlannedContainerClick> clicks,
            List<ItemStack> desired
    ) {
        List<ItemStack> actual = current.stream()
                .map(ItemStack::copy)
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll);
        ItemStack cursor = ItemStack.EMPTY;

        for (PlannedContainerClick click : clicks) {
            Assertions.assertEquals(ContainerInput.PICKUP, click.input());
            Assertions.assertEquals(0, click.button());
            cursor = applyPickupClick(actual, cursor, click.slotIndex());
        }

        Assertions.assertTrue(cursor.isEmpty(), "planned clicks must leave the cursor empty");
        assertSameLayout(actual, desired);
    }

    private static ItemStack applyPickupClick(List<ItemStack> slots, ItemStack cursor, int slotIndex) {
        ItemStack slot = slots.get(slotIndex);
        if (cursor.isEmpty()) {
            slots.set(slotIndex, ItemStack.EMPTY);
            return slot;
        }

        if (slot.isEmpty()) {
            slots.set(slotIndex, cursor);
            return ItemStack.EMPTY;
        }

        if (canMergePickup(cursor, slot)) {
            int moved = Math.min(cursor.getCount(), slot.getMaxStackSize() - slot.getCount());
            slot.grow(moved);
            cursor.shrink(moved);
            return cursor.isEmpty() ? ItemStack.EMPTY : cursor;
        }

        slots.set(slotIndex, cursor);
        return slot;
    }

    private static boolean canMergePickup(ItemStack cursor, ItemStack slot) {
        return ItemStack.isSameItemSameComponents(cursor, slot)
                && cursor.isStackable()
                && slot.isStackable()
                && slot.getCount() < slot.getMaxStackSize();
    }

    private static void assertSameLayout(List<ItemStack> actual, List<ItemStack> expected) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertTrue(
                    sameLayoutStack(actual.get(i), expected.get(i)),
                    "slot " + i + " expected " + expected.get(i) + " but was " + actual.get(i)
            );
        }
    }

    private static boolean sameLayoutStack(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
    }

    private static PlannedContainerClick click(int slot) {
        return new PlannedContainerClick(slot, 0, ContainerInput.PICKUP);
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }
}
