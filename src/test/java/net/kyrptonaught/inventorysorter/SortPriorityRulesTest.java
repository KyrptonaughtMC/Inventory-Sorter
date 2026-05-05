package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

public class SortPriorityRulesTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void itemIdRuleCanMoveMatchingStacksFirst() {
        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.FIRST)),
                stack(Items.APPLE),
                bundle(),
                stack(Items.DIAMOND)
        );

        Assertions.assertTrue(sorted.get(0).is(Items.BUNDLE));
    }

    @Test
    void componentRulesUseMinecraftRegistry() {
        List<ItemStack> sorted = sort(
                List.of(
                        new SortPriorityRuleSetting("@minecraft:bundle_contents", SortPriorityPosition.LAST)
                ),
                bundle(),
                stack(Items.APPLE),
                stack(Items.WHITE_SHULKER_BOX)
        );

        Assertions.assertTrue(sorted.get(2).is(Items.BUNDLE));
    }

    @Test
    void tagExpressionsAreValidRules() {
        Assertions.assertTrue(SortPriorityRules.validationError("#minecraft:shulker_boxes").isEmpty());
    }

    @Test
    void logicalOperatorsCanExcludeSpecificMatches() {
        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("@minecraft:bundle_contents & !minecraft:apple", SortPriorityPosition.FIRST)),
                stack(Items.WHITE_SHULKER_BOX),
                bundle(),
                stack(Items.APPLE)
        );

        Assertions.assertTrue(sorted.get(0).is(Items.BUNDLE));
    }

    @Test
    void ruleOrderBreaksTiesInsideTheSamePriorityBucket() {
        List<ItemStack> sorted = sort(
                List.of(
                        new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.FIRST),
                        new SortPriorityRuleSetting("minecraft:white_shulker_box", SortPriorityPosition.FIRST)
                ),
                stack(Items.WHITE_SHULKER_BOX),
                bundle()
        );

        Assertions.assertTrue(sorted.get(0).is(Items.BUNDLE));
        Assertions.assertTrue(sorted.get(1).is(Items.WHITE_SHULKER_BOX));
    }

    @Test
    void nameRulesMatchDisplayNamesWithFullStringGlobs() {
        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("name:\"Meza's *\"", SortPriorityPosition.LAST)),
                namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                namedStack(Items.APPLE, "Apple"),
                namedStack(Items.FEATHER, "Feather")
        );

        Assertions.assertTrue(sorted.get(0).is(Items.APPLE));
        Assertions.assertTrue(sorted.get(1).is(Items.FEATHER));
        Assertions.assertTrue(sorted.get(2).is(Items.DIAMOND_PICKAXE));
    }

    @Test
    void nameRulesAreCaseInsensitiveAndDoNotTreatPlainTextAsContains() {
        List<ItemStack> exactRuleSorted = sort(
                List.of(new SortPriorityRuleSetting("name:\"meza's pickaxe\"", SortPriorityPosition.FIRST)),
                namedStack(Items.APPLE, "Apple"),
                namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe")
        );
        List<ItemStack> nonContainsRuleSorted = sort(
                List.of(new SortPriorityRuleSetting("name:\"Meza's\"", SortPriorityPosition.FIRST)),
                namedStack(Items.APPLE, "Apple"),
                namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe")
        );

        Assertions.assertTrue(exactRuleSorted.get(0).is(Items.DIAMOND_PICKAXE));
        Assertions.assertTrue(nonContainsRuleSorted.get(0).is(Items.APPLE));
    }

    @Test
    void nameRulesCanEscapeWildcardCharacters() {
        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("name:\"Meza's \\* Pickaxe\"", SortPriorityPosition.FIRST)),
                namedStack(Items.APPLE, "Apple"),
                namedStack(Items.DIAMOND_PICKAXE, "Meza's * Pickaxe"),
                namedStack(Items.IRON_PICKAXE, "Meza's Fast Pickaxe")
        );

        Assertions.assertTrue(sorted.get(0).is(Items.DIAMOND_PICKAXE));
        Assertions.assertTrue(sorted.get(1).is(Items.APPLE));
        Assertions.assertTrue(sorted.get(2).is(Items.IRON_PICKAXE));
    }

    @Test
    void invalidNameRulesAreReportedAndIgnoredAtRuntime() {
        Assertions.assertTrue(SortPriorityRules.validationError("name:\"Meza's *").isPresent());

        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("name:\"Meza's *", SortPriorityPosition.FIRST)),
                namedStack(Items.FEATHER, "Feather"),
                namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe"),
                namedStack(Items.APPLE, "Apple")
        );

        Assertions.assertTrue(sorted.get(0).is(Items.APPLE));
        Assertions.assertTrue(sorted.get(1).is(Items.DIAMOND_PICKAXE));
        Assertions.assertTrue(sorted.get(2).is(Items.FEATHER));
    }

    @Test
    void anyMatchingIgnoreRuleExcludesAStackFromSorting() {
        SortPriorityRules firstThenIgnore = SortPriorityRules.compile(List.of(
                new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.FIRST),
                new SortPriorityRuleSetting("@minecraft:bundle_contents", SortPriorityPosition.IGNORE)
        ));
        SortPriorityRules ignoreThenFirst = SortPriorityRules.compile(List.of(
                new SortPriorityRuleSetting("@minecraft:bundle_contents", SortPriorityPosition.IGNORE),
                new SortPriorityRuleSetting("minecraft:bundle", SortPriorityPosition.FIRST)
        ));

        Assertions.assertTrue(firstThenIgnore.shouldIgnore(bundle()));
        Assertions.assertTrue(ignoreThenFirst.shouldIgnore(bundle()));
        Assertions.assertFalse(ignoreThenFirst.shouldIgnore(stack(Items.APPLE)));
    }

    @Test
    void invalidExpressionsAreReportedForTheConfigUiAndIgnoredAtRuntime() {
        Assertions.assertTrue(SortPriorityRules.validationError("@").isPresent());

        List<ItemStack> sorted = sort(
                List.of(new SortPriorityRuleSetting("@", SortPriorityPosition.FIRST)),
                stack(Items.DIAMOND),
                stack(Items.APPLE)
        );

        Assertions.assertTrue(sorted.get(0).is(Items.APPLE));
        Assertions.assertTrue(sorted.get(1).is(Items.DIAMOND));
    }

    private static List<ItemStack> sort(List<SortPriorityRuleSetting> rules, ItemStack... stacks) {
        List<ItemStack> sorted = new java.util.ArrayList<>(List.of(stacks));
        sorted.sort(SortPriorityRules.compile(rules)
                .applyTo(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())));
        return sorted;
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(
                Holder.direct(item),
                1,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = stack(item);
        stack.set(DataComponents.ITEM_NAME, Component.literal(name));
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
