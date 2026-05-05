package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minecraft.core.component.DataComponents.ITEM_NAME;

public class SortedInventoryLayoutTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void emptyInventoryRemainsFixedSizeAndEmpty() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY),
                SortType.NAME,
                "en_us"
        );

        Assertions.assertEquals(3, layout.stacks().size());
        Assertions.assertTrue(layout.stacks().stream().allMatch(ItemStack::isEmpty));
    }

    @Test
    void partialStacksMergeAndLeaveEmptySlotsAtEnd() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 32),
                        ItemStack.EMPTY,
                        stack(Items.DIAMOND, 33)
                ),
                SortType.NAME,
                "en_us"
        );

        assertStack(layout.stacks().get(0), Items.DIAMOND, 64);
        assertStack(layout.stacks().get(1), Items.DIAMOND, 1);
        Assertions.assertTrue(layout.stacks().get(2).isEmpty());
    }

    @Test
    void incompatibleComponentsDoNotMerge() {
        ItemStack namedDiamond = stack(Items.DIAMOND, 30);
        namedDiamond.set(ITEM_NAME, Component.literal("Named Diamond"));

        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 30),
                        namedDiamond,
                        stack(Items.DIAMOND, 30)
                ),
                SortType.NAME,
                "en_us"
        );

        assertStack(layout.stacks().get(0), Items.DIAMOND, 60);
        assertStack(layout.stacks().get(1), Items.DIAMOND, 30);
        Assertions.assertEquals("Named Diamond", layout.stacks().get(1).getHoverName().getString());
        Assertions.assertTrue(layout.stacks().get(2).isEmpty());
    }

    @Test
    void nameSortUsesExistingComparator() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 1, "Diamond"),
                        stack(Items.CACTUS, 1, "Cactus"),
                        stack(Items.APPLE, 1, "Apple")
                ),
                SortType.NAME,
                "en_us"
        );

        assertStack(layout.stacks().get(0), Items.APPLE, 1);
        assertStack(layout.stacks().get(1), Items.CACTUS, 1);
        assertStack(layout.stacks().get(2), Items.DIAMOND, 1);
    }

    @Test
    void priorityRulesWrapExistingComparatorWithoutChangingMergeRules() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 32, "Diamond"),
                        bundle(),
                        stack(Items.DIAMOND, 32, "Diamond")
                ),
                SortType.NAME,
                "en_us",
                List.of(new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.FIRST))
        );

        assertStack(layout.stacks().get(0), Items.BUNDLE, 1);
        assertStack(layout.stacks().get(1), Items.DIAMOND, 64);
        Assertions.assertTrue(layout.stacks().get(2).isEmpty());
    }

    @Test
    void ignoredStacksKeepTheirSlotsWhileOtherStacksSortAroundThem() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 1, "Diamond"),
                        stack(Items.WHITE_SHULKER_BOX, 1, "Shulker Box"),
                        stack(Items.APPLE, 1, "Apple"),
                        stack(Items.FEATHER, 1, "Feather")
                ),
                SortType.NAME,
                "en_us",
                List.of(new SortPriorityRuleSetting("minecraft:white_shulker_box", SortPriorityPosition.IGNORE))
        );

        assertStack(layout.stacks().get(0), Items.APPLE, 1);
        assertStack(layout.stacks().get(1), Items.WHITE_SHULKER_BOX, 1);
        assertStack(layout.stacks().get(2), Items.DIAMOND, 1);
        assertStack(layout.stacks().get(3), Items.FEATHER, 1);
    }

    @Test
    void ignoredStacksDoNotMergeWithEachOther() {
        SortedInventoryLayout layout = SortedInventoryLayout.from(
                List.of(
                        stack(Items.DIAMOND, 32),
                        ItemStack.EMPTY,
                        stack(Items.DIAMOND, 32)
                ),
                SortType.NAME,
                "en_us",
                List.of(new SortPriorityRuleSetting("minecraft:diamond", SortPriorityPosition.IGNORE))
        );

        assertStack(layout.stacks().get(0), Items.DIAMOND, 32);
        Assertions.assertTrue(layout.stacks().get(1).isEmpty());
        assertStack(layout.stacks().get(2), Items.DIAMOND, 32);
    }

    @Test
    void inputStacksAreCopiedBeforeMerging() {
        ItemStack first = stack(Items.DIAMOND, 32);
        ItemStack second = stack(Items.DIAMOND, 32);

        SortedInventoryLayout.from(List.of(first, second), SortType.NAME, "en_us");

        assertStack(first, Items.DIAMOND, 32);
        assertStack(second, Items.DIAMOND, 32);
    }

    private static void assertStack(ItemStack stack, Item item, int count) {
        Assertions.assertTrue(stack.is(item), "Expected " + item + " but got " + stack.getItem());
        Assertions.assertEquals(count, stack.getCount(), "Expected " + count + " " + item + " but got " + stack.getCount() + " " + stack.getItem());
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    private static ItemStack stack(Item item, int count, String name) {
        ItemStack stack = stack(item, count);
        stack.set(ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack bundle() {
        return new ItemStack(
                Holder.direct(Items.BUNDLE),
                1,
                DataComponentPatch.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 1)
                        .set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                        .build()
        );
    }
}
