package net.kyrptonaught.inventorysorter.sort.bundle;

import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class BundleTargetSlotsTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void flattensMultipleTargetContainersInContainerOrder() {
        SimpleContainer hotbar = new SimpleContainer(stack(Items.APPLE), stack(Items.DIAMOND));
        SimpleContainer trinkets = new SimpleContainer(stack(Items.STRING));

        BundleTargetSlots targets = BundleTargetSlots.fromContainers(List.of(hotbar, trinkets));

        assertSameLayoutStacks(List.of(stack(Items.APPLE), stack(Items.DIAMOND), stack(Items.STRING)), targets.stacks());
    }

    @Test
    void writesAdjustedStacksBackToTheirOriginalContainers() {
        SimpleContainer hotbar = new SimpleContainer(stack(Items.APPLE));
        SimpleContainer trinkets = new SimpleContainer(stack(Items.STRING), stack(Items.DIAMOND));

        BundleTargetSlots targets = BundleTargetSlots.fromContainers(List.of(hotbar, trinkets));
        targets.setStacks(List.of(stack(Items.GOLD_INGOT), stack(Items.EMERALD), stack(Items.IRON_INGOT)));

        assertSameLayoutStack(stack(Items.GOLD_INGOT), hotbar.getItem(0));
        assertSameLayoutStack(stack(Items.EMERALD), trinkets.getItem(0));
        assertSameLayoutStack(stack(Items.IRON_INGOT), trinkets.getItem(1));
    }

    @Test
    void writesAdjustedStacksBackToSlotAccessTargets() {
        ItemStack[] targetStack = {stack(Items.APPLE)};
        //? if fabric {
        MutableSlotAccess target = new MutableSlotAccess(targetStack[0]);
        //?}
        //? if neoforge {
        /*SlotAccess target = SlotAccess.of(() -> targetStack[0], stack -> targetStack[0] = stack);
        *///?}
        boolean[] changed = {false};

        BundleTargetSlots targets = BundleTargetSlots.fromSlots(List.of(new BundleTargetSlot(target, () -> changed[0] = true)));
        targets.setStacks(List.of(stack(Items.GOLD_INGOT)));

        //? if fabric {
        assertSameLayoutStack(stack(Items.GOLD_INGOT), target.stack);
        //?}
        //? if neoforge {
        /*assertSameLayoutStack(stack(Items.GOLD_INGOT), targetStack[0]);
        *///?}
        Assertions.assertTrue(changed[0]);
    }

    @Test
    void rejectsMismatchedStackCountWhenWritingBack() {
        SimpleContainer hotbar = new SimpleContainer(stack(Items.APPLE), stack(Items.DIAMOND));

        BundleTargetSlots targets = BundleTargetSlots.fromContainers(List.of(hotbar));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> targets.setStacks(List.of(stack(Items.GOLD_INGOT)))
        );
    }

    private static void assertSameLayoutStacks(List<ItemStack> expected, List<ItemStack> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertSameLayoutStack(expected.get(i), actual.get(i));
        }
    }

    private static void assertSameLayoutStack(ItemStack expected, ItemStack actual) {
        Assertions.assertTrue(
                SortableItemStackRules.sameLayoutStack(expected, actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }

    private static ItemStack stack(net.minecraft.world.item.Item item) {
        return new ItemStack(
                Holder.direct(item),
                1,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    //? if fabric {
    private static class MutableSlotAccess implements SlotAccess {
        private ItemStack stack;

        private MutableSlotAccess(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack get() {
            return stack;
        }

        @Override
        public boolean set(ItemStack stack) {
            this.stack = stack;
            return true;
        }
    }
    //? }
}
