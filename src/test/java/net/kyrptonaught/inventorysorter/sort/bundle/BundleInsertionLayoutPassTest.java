package net.kyrptonaught.inventorysorter.sort.bundle;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortableItemStackRules;
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
import java.util.Map;
import java.util.stream.Collectors;

class BundleInsertionLayoutPassTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void movesMatchingLooseItemsIntoExistingBundle() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 8)),
                stack(Items.DIAMOND, 1),
                stack(Items.APPLE, 12)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.APPLE, 20), bundleContents(output.get(0)));
        Assertions.assertTrue(output.get(2).isEmpty());
    }

    @Test
    void leavesDifferentItemsOutsideBundle() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 8)),
                stack(Items.STRING, 12)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.APPLE, 8), bundleContents(output.get(0)));
        assertSameLayoutStack(stack(Items.STRING, 12), output.get(1));
    }

    @Test
    void returnsCopiedStacksWhenThereAreNoBundleTargets() {
        ItemStack apple = stack(Items.APPLE, 12);
        BundleInsertionLayoutPass.Result output = BundleInsertionLayoutPass.apply(
                List.of(apple),
                List.of(stack(Items.DIAMOND, 1)),
                SortPriorityRules.EMPTY
        );

        assertSameLayoutStack(apple, output.layoutStacks().getFirst());
        assertSameLayoutStack(stack(Items.DIAMOND, 1), output.extraTargetStacks().getFirst());
        Assertions.assertNotSame(apple, output.layoutStacks().getFirst());
    }

    @Test
    void canUseExtraBundleTargetsWithoutAddingThemToTheLayout() {
        BundleInsertionLayoutPass.Result output = BundleInsertionLayoutPass.apply(
                List.of(
                        stack(Items.APPLE, 12),
                        stack(Items.DIAMOND, 1)
                ),
                List.of(bundleContaining(stack(Items.APPLE, 8))),
                SortPriorityRules.EMPTY
        );

        Assertions.assertTrue(output.layoutStacks().get(0).isEmpty());
        assertSameLayoutStack(stack(Items.DIAMOND, 1), output.layoutStacks().get(1));
        Assertions.assertEquals(Map.of(Items.APPLE, 20), bundleContents(output.extraTargetStacks().get(0)));
        Assertions.assertEquals(List.of(new BundleInsertionLayoutPass.BundleInsertion(
                0,
                List.of(new BundleInsertionLayoutPass.BundleInsertionTarget(BundleInsertionLayoutPass.TargetArea.EXTRA_TARGET, 0))
        )), output.insertions());
    }

    @Test
    void recordsLayoutBundleTargetsInInsertionOrder() {
        BundleInsertionLayoutPass.Result output = BundleInsertionLayoutPass.apply(
                List.of(
                        bundleContaining(stack(Items.APPLE, 62)),
                        bundleContaining(stack(Items.APPLE, 40)),
                        stack(Items.APPLE, 10)
                ),
                List.of(),
                SortPriorityRules.EMPTY
        );

        Assertions.assertEquals(List.of(new BundleInsertionLayoutPass.BundleInsertion(
                2,
                List.of(
                        new BundleInsertionLayoutPass.BundleInsertionTarget(BundleInsertionLayoutPass.TargetArea.LAYOUT, 0),
                        new BundleInsertionLayoutPass.BundleInsertionTarget(BundleInsertionLayoutPass.TargetArea.LAYOUT, 1)
                )
        )), output.insertions());
    }

    @Test
    void stopsCheckingTargetsWhenCandidateWasFullyInserted() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 8)),
                bundleContaining(stack(Items.APPLE, 8)),
                stack(Items.APPLE, 3)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.APPLE, 11), bundleContents(output.get(0)));
        Assertions.assertEquals(Map.of(Items.APPLE, 8), bundleContents(output.get(1)));
        Assertions.assertTrue(output.get(2).isEmpty());
    }

    @Test
    void usesSmallestMatchingStackFirstWhenSpaceIsLimited() {
        ItemStack small = namedStack(Items.APPLE, "Small", 2);
        ItemStack large = namedStack(Items.APPLE, "Large", 4);

        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(namedStack(Items.APPLE, "Small", 10), namedStack(Items.APPLE, "Large", 50)),
                large,
                small
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(
                "Small", 12,
                "Large", 52
        ), namedBundleContents(output.get(0)));
        assertSameLayoutStack(namedStack(Items.APPLE, "Large", 2), output.get(1));
        Assertions.assertTrue(output.get(2).isEmpty());
    }

    @Test
    void insertsIntoMatchingBundlesInSlotOrder() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 62)),
                bundleContaining(stack(Items.APPLE, 40)),
                stack(Items.APPLE, 10)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.APPLE, 64), bundleContents(output.get(0)));
        Assertions.assertEquals(Map.of(Items.APPLE, 48), bundleContents(output.get(1)));
        Assertions.assertTrue(output.get(2).isEmpty());
    }

    @Test
    void doesNotMoveIgnoredItemsIntoBundles() {
        SortPriorityRules priorityRules = SortPriorityRules.compile(List.of(
                new SortPriorityRuleSetting("minecraft:apple", SortPriorityPosition.IGNORE)
        ));

        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 8)),
                stack(Items.APPLE, 5)
        ), priorityRules);

        Assertions.assertEquals(Map.of(Items.APPLE, 8), bundleContents(output.get(0)));
        assertSameLayoutStack(stack(Items.APPLE, 5), output.get(1));
    }

    @Test
    void doesNotUseEmptyOrFullBundlesAsTargets() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(),
                bundleContaining(stack(Items.APPLE, 64)),
                stack(Items.APPLE, 5)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(), bundleContents(output.get(0)));
        Assertions.assertEquals(Map.of(Items.APPLE, 64), bundleContents(output.get(1)));
        assertSameLayoutStack(stack(Items.APPLE, 5), output.get(2));
    }

    @Test
    void ignoresEmptyCandidateStacksWhenBundleTargetsExist() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.APPLE, 8)),
                ItemStack.EMPTY
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.APPLE, 8), bundleContents(output.get(0)));
        Assertions.assertTrue(output.get(1).isEmpty());
    }

    @Test
    void doesNotTreatNestedBundleContentsAsDirectContents() {
        ItemStack nestedStringBundle = bundleContaining(bundleContaining(stack(Items.STRING, 5)));

        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                nestedStringBundle,
                stack(Items.STRING, 5)
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.BUNDLE, 1), bundleContents(output.get(0)));
        assertSameLayoutStack(stack(Items.STRING, 5), output.get(1));
    }

    @Test
    void doesNotMoveNonEmptyBundleIntoAnotherBundle() {
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                bundleContaining(stack(Items.BUNDLE, 1)),
                bundleContaining(stack(Items.STRING, 5))
        ), SortPriorityRules.EMPTY);

        Assertions.assertEquals(Map.of(Items.BUNDLE, 1), bundleContents(output.get(0)));
        Assertions.assertEquals(Map.of(Items.STRING, 5), bundleContents(output.get(1)));
    }

    @Test
    void sortedInventoryLayoutCanApplyBundleInsertionBeforeTopLevelSorting() {
        ItemStack appleBundle = bundleContaining(stack(Items.APPLE, 44));

        List<ItemStack> output = SortedInventoryLayout.from(
                List.of(appleBundle, stack(Items.APPLE, 30), stack(Items.APPLE, 6), stack(Items.DIAMOND, 2)),
                SortType.NAME,
                "en_us",
                List.of(),
                true
        ).stacks();

        Assertions.assertEquals(Map.of(Items.APPLE, 64), bundleContents(output.get(0)));
        assertSameLayoutStack(stack(Items.APPLE, 16), output.get(1));
        assertSameLayoutStack(stack(Items.DIAMOND, 2), output.get(2));
    }

    @Test
    void sortedInventoryLayoutCanContinueFromBundleAdjustedStacks() {
        List<ItemStack> output = SortedInventoryLayout.fromBundleAdjusted(
                List.of(ItemStack.EMPTY, stack(Items.DIAMOND, 2), stack(Items.STICK, 4)),
                SortType.NAME,
                "en_us",
                List.of()
        ).stacks();

        assertSameLayoutStack(stack(Items.STICK, 4), output.get(0));
        assertSameLayoutStack(stack(Items.DIAMOND, 2), output.get(1));
        Assertions.assertTrue(output.get(2).isEmpty());
    }

    @Test
    void explicitPriorityRulesStillWinOverBundleTargetOrdering() {
        ItemStack appleBundle = bundleContaining(stack(Items.APPLE, 8));

        List<ItemStack> output = SortedInventoryLayout.from(
                List.of(appleBundle, stack(Items.WHITE_SHULKER_BOX, 1), stack(Items.APPLE, 5), stack(Items.DIAMOND, 1)),
                SortType.NAME,
                "en_us",
                List.of(new SortPriorityRuleSetting("minecraft:white_shulker_box", SortPriorityPosition.FIRST)),
                true
        ).stacks();

        assertSameLayoutStack(stack(Items.WHITE_SHULKER_BOX, 1), output.get(0));
        Assertions.assertEquals(Map.of(Items.APPLE, 13), bundleContents(output.get(1)));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), output.get(2));
    }

    @Test
    void lowStackSizeRemaindersUseNormalTopLevelOrderingAfterBundleInsertion() {
        ItemStack eggBundle = bundleContaining(stack(Items.EGG, 12));
        List<ItemStack> output = BundleInsertionLayoutPass.apply(List.of(
                eggBundle,
                stack(Items.EGG, 6),
                stack(Items.EGG, 2),
                stack(Items.DIAMOND, 1)
        ), SortPriorityRules.EMPTY);
        output = output.stream()
                .filter(stack -> !stack.isEmpty())
                .sorted(BundleInsertionLayoutPass.targetAwareOrdering(output, java.util.Comparator.comparing(stack -> stack.getItem().getDescriptionId())))
                .toList();

        Assertions.assertEquals(Map.of(Items.EGG, 16), bundleContents(output.get(0)));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), output.get(1));
        assertSameLayoutStack(stack(Items.EGG, 4), output.get(2));
    }

    @Test
    void emptyBundlesDoNotContributeTargetAwareOrderingContents() {
        List<ItemStack> output = List.of(
                bundleContaining(),
                stack(Items.DIAMOND, 1)
        ).stream()
                .sorted(BundleInsertionLayoutPass.targetAwareOrdering(List.of(bundleContaining()), java.util.Comparator.comparing(stack -> stack.getItem().getDescriptionId())))
                .toList();

        assertSameLayoutStack(bundleContaining(), output.get(0));
        assertSameLayoutStack(stack(Items.DIAMOND, 1), output.get(1));
    }

    private static ItemStack stack(Item item, int count) {
        int maxStackSize = 64;
        if (item == Items.BUNDLE) {
            maxStackSize = 1;
        } else if (item == Items.EGG) {
            maxStackSize = 16;
        }
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, maxStackSize).build()
        );
    }

    private static ItemStack namedStack(Item item, String name, int count) {
        ItemStack stack = stack(item, count);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack bundleContaining(ItemStack... contents) {
        BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
        for (ItemStack content : contents) {
            mutable.tryInsert(content.copy());
        }

        ItemStack bundle = stack(Items.BUNDLE, 1);
        bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return bundle;
    }

    private static Map<Item, Integer> bundleContents(ItemStack bundle) {
        return bundle.get(DataComponents.BUNDLE_CONTENTS)
                .itemCopyStream()
                .collect(Collectors.toMap(ItemStack::getItem, ItemStack::getCount, Integer::sum));
    }

    private static Map<String, Integer> namedBundleContents(ItemStack bundle) {
        return bundle.get(DataComponents.BUNDLE_CONTENTS)
                .itemCopyStream()
                .collect(Collectors.toMap(stack -> stack.getHoverName().getString(), ItemStack::getCount, Integer::sum));
    }

    private static void assertSameLayoutStack(ItemStack expected, ItemStack actual) {
        Assertions.assertTrue(
                SortableItemStackRules.sameLayoutStack(expected, actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }
}
