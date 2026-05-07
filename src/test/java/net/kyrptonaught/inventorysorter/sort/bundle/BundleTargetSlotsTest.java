package net.kyrptonaught.inventorysorter.sort.bundle;

import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
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
}
