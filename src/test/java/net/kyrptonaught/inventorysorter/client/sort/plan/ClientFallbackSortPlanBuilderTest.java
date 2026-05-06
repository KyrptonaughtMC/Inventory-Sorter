package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.client.sort.ClientSortScope;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ClientFallbackSortPlanBuilderTest {
    private final ClientFallbackSortPlanBuilder builder = new ClientFallbackSortPlanBuilder(new ClientSortClickPlanner());

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void buildsPlainLayoutSortWhenBundleSortingIsDisabled() {
        SimpleContainer container = new SimpleContainer(namedStack(Items.DIAMOND, "Diamond"), namedStack(Items.APPLE, "Apple"));

        List<PlannedContainerClick> clicks = builder.build(
                scope(container),
                SortType.NAME,
                "en_us",
                List.of(),
                false,
                false
        ).orElseThrow();

        Assertions.assertFalse(clicks.isEmpty());
    }

    @Test
    void prependsBundleInsertionClicksBeforeLayoutSort() {
        SimpleContainer container = new SimpleContainer(3);
        container.setItem(0, bundleContaining(stack(Items.APPLE, 8)));
        container.setItem(1, stack(Items.DIAMOND, 1));
        container.setItem(2, stack(Items.APPLE, 6));

        List<PlannedContainerClick> clicks = builder.build(
                scope(container),
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                false
        ).orElseThrow();

        Assertions.assertEquals(List.of(pickupClick(12), pickupClick(10)), clicks.subList(0, 2));
        List<ItemStack> actual = stacks(container);
        applyClicks(actual, clicks);
        Assertions.assertEquals(14, bundleContentsCount(actual.get(0)));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), actual.get(1));
        Assertions.assertTrue(actual.get(2).isEmpty());
    }

    @Test
    void canUseHotbarBundleTargetsWhenEnabled() {
        SimpleContainer mainInventory = new SimpleContainer(1);
        mainInventory.setItem(0, stack(Items.APPLE, 6));
        SimpleContainer hotbar = new SimpleContainer(bundleContaining(stack(Items.APPLE, 8)));

        List<PlannedContainerClick> clicks = builder.build(
                scope(mainInventory, hotbar),
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                true
        ).orElseThrow();

        Assertions.assertEquals(List.of(pickupClick(10), pickupClick(20)), clicks);
    }

    @Test
    void doesNotUseHotbarBundleTargetsWhenDisabled() {
        SimpleContainer mainInventory = new SimpleContainer(1);
        mainInventory.setItem(0, stack(Items.APPLE, 6));
        SimpleContainer hotbar = new SimpleContainer(bundleContaining(stack(Items.APPLE, 8)));

        List<PlannedContainerClick> clicks = builder.build(
                scope(mainInventory, hotbar),
                SortType.NAME,
                "en_us",
                List.of(),
                true,
                false
        ).orElseThrow();

        Assertions.assertTrue(clicks.isEmpty());
    }

    @Test
    void returnsEmptyWhenLayoutClicksCannotBePlanned() {
        ClientFallbackSortPlanBuilder rejectingBuilder = new ClientFallbackSortPlanBuilder(new RejectingClickPlanner());
        SimpleContainer container = new SimpleContainer(namedStack(Items.DIAMOND, "Diamond"), namedStack(Items.APPLE, "Apple"));

        Assertions.assertTrue(rejectingBuilder.build(
                scope(container),
                SortType.NAME,
                "en_us",
                List.of(),
                false,
                false
        ).isEmpty());
    }

    private static ClientSortScope scope(Container layout) {
        return scope(layout, new SimpleContainer(0));
    }

    private static ClientSortScope scope(Container layout, Container hotbar) {
        List<ClientSortScope.ScopedSlot> layoutSlots = new ArrayList<>();
        for (int i = 0; i < layout.getContainerSize(); i++) {
            layoutSlots.add(new ClientSortScope.ScopedSlot(10 + i, new Slot(layout, i, 0, 0)));
        }

        List<ClientSortScope.ScopedSlot> hotbarSlots = new ArrayList<>();
        for (int i = 0; i < hotbar.getContainerSize(); i++) {
            hotbarSlots.add(new ClientSortScope.ScopedSlot(20 + i, new Slot(hotbar, i, 0, 0)));
        }
        return new ClientSortScope(4, layoutSlots, hotbarSlots);
    }

    private static List<ItemStack> stacks(Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            stacks.add(container.getItem(i).copy());
        }
        return stacks;
    }

    private static void applyClicks(List<ItemStack> actual, List<PlannedContainerClick> clicks) {
        ItemStack cursor = ItemStack.EMPTY;
        for (PlannedContainerClick click : clicks) {
            cursor = applyPickupClick(actual, cursor, click.slotIndex() - 10);
        }
        Assertions.assertTrue(cursor.isEmpty());
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

        BundleContents contents = slot.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && slot.getCount() == 1) {
            BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
            int inserted = mutable.tryInsert(cursor);
            if (inserted > 0) {
                slot.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                return cursor.isEmpty() ? ItemStack.EMPTY : cursor;
            }
        }

        slots.set(slotIndex, cursor);
        return slot;
    }

    private static int bundleContentsCount(ItemStack bundle) {
        return bundle.get(DataComponents.BUNDLE_CONTENTS)
                .itemCopyStream()
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static void assertSameLayoutStack(ItemStack expected, ItemStack actual) {
        Assertions.assertTrue(
                expected.getCount() == actual.getCount() && ItemStack.isSameItemSameComponents(expected, actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }

    private static PlannedContainerClick pickupClick(int slot) {
        return new PlannedContainerClick(slot, 0, ContainerInput.PICKUP);
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = stack(item, 1);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack bundleContaining(ItemStack... contents) {
        BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
        for (ItemStack content : contents) {
            mutable.tryInsert(content.copy());
        }

        ItemStack bundle = new ItemStack(
                Holder.direct(Items.BUNDLE),
                1,
                DataComponentPatch.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 1)
                        .set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable())
                        .build()
        );
        return bundle;
    }

    private static class RejectingClickPlanner extends ClientSortClickPlanner {
        @Override
        public java.util.Optional<List<PlannedContainerClick>> plan(List<SlotState> current, List<ItemStack> desired) {
            return java.util.Optional.empty();
        }
    }
}
