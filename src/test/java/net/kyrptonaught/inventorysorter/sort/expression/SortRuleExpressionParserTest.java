package net.kyrptonaught.inventorysorter.sort.expression;

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

public class SortRuleExpressionParserTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void itemExpressionsMatchFullOrImplicitMinecraftIds() {
        SortRuleExpression fullId = parse("minecraft:diamond");
        SortRuleExpression implicitMinecraftId = parse("diamond");

        Assertions.assertTrue(matches(fullId, stack(Items.DIAMOND)));
        Assertions.assertTrue(matches(implicitMinecraftId, stack(Items.DIAMOND)));
        Assertions.assertFalse(matches(fullId, stack(Items.APPLE)));
    }

    @Test
    void tagExpressionsAreValidRuleExpressions() {
        Assertions.assertTrue(SortRuleExpressionParser.parse("#minecraft:shulker_boxes").isPresent());
        Assertions.assertTrue(SortRuleExpressionParser.validationError("#minecraft:shulker_boxes").isEmpty());
    }

    @Test
    void componentExpressionsMatchDataComponents() {
        SortRuleExpression expression = parse("@minecraft:bundle_contents");

        Assertions.assertTrue(matches(expression, bundle()));
        Assertions.assertFalse(matches(expression, stack(Items.APPLE)));
    }

    @Test
    void nameExpressionsMatchCaseInsensitiveFullStringGlobs() {
        SortRuleExpression expression = parse("name:\"Meza's *\"");

        Assertions.assertTrue(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe")));
        Assertions.assertTrue(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "meza's silk pickaxe")));
        Assertions.assertFalse(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Fast Meza's Pickaxe")));
    }

    @Test
    void nameExpressionsCanEscapeWildcardCharacters() {
        SortRuleExpression expression = parse("name:\"Meza's \\* Pickaxe\"");

        Assertions.assertTrue(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Meza's * Pickaxe")));
        Assertions.assertFalse(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Meza's Fast Pickaxe")));
    }

    @Test
    void logicalExpressionsRespectPrecedenceAndParentheses() {
        SortRuleExpression withoutParentheses = parse("minecraft:apple | minecraft:diamond & !name:\"Meza's *\"");
        SortRuleExpression withParentheses = parse("(minecraft:apple | minecraft:diamond) & !name:\"Meza's *\"");

        Assertions.assertTrue(matches(withoutParentheses, stack(Items.APPLE)));
        Assertions.assertFalse(matches(withParentheses, namedStack(Items.APPLE, "Meza's Apple")));
        Assertions.assertFalse(matches(withoutParentheses, namedStack(Items.DIAMOND, "Meza's Diamond")));
        Assertions.assertTrue(matches(withParentheses, stack(Items.DIAMOND)));
    }

    @Test
    void invalidExpressionsReturnValidationErrorsAndNoCompiledExpression() {
        Assertions.assertTrue(SortRuleExpressionParser.parse("@").isEmpty());
        Assertions.assertTrue(SortRuleExpressionParser.validationError("@").isPresent());
        Assertions.assertTrue(SortRuleExpressionParser.parse("name:\"Meza's *").isEmpty());
        Assertions.assertTrue(SortRuleExpressionParser.validationError("name:\"Meza's *").isPresent());
    }

    @Test
    void invalidExpressionsCoverParserErrorBranches() {
        assertInvalid(null, "Expected a rule atom at position 0");
        assertInvalid("minecraft:apple minecraft:diamond", "Unexpected token at position 16");
        assertInvalid("(minecraft:apple", "Expected ')' at position 16");
        assertInvalid("!", "Expected a rule atom at position 1");
        assertInvalid("@minecraft:not_a_real_component", "Unknown data component: minecraft:not_a_real_component at position 31");
        assertInvalid("name:Meza", "Expected quoted name glob at position 5");
        assertInvalid("name:", "Expected quoted name glob at position 5");
        assertInvalid("name:\"Meza\\", "Expected escaped character at position 11");
        assertInvalid("minecraft:bad id", "Unexpected token at position 14");
        assertInvalid("minecraft:Apple", "Invalid identifier: minecraft:Apple at position 15");
    }

    @Test
    void parserAcceptsWhitespaceAndIdentifierCharactersUsedByMinecraftIds() {
        Assertions.assertTrue(SortRuleExpressionParser.parse("  ( minecraft:diamond_sword | minecraft:stone/bricks )  ").isPresent());
        Assertions.assertTrue(SortRuleExpressionParser.parse("minecraft:blue-stained.glass").isPresent());
    }

    private static SortRuleExpression parse(String expression) {
        return SortRuleExpressionParser.parse(expression).orElseThrow();
    }

    private static void assertInvalid(String expression, String expectedMessage) {
        Assertions.assertTrue(SortRuleExpressionParser.parse(expression).isEmpty());
        Assertions.assertEquals(expectedMessage, SortRuleExpressionParser.validationError(expression).orElseThrow());
    }

    private static boolean matches(SortRuleExpression expression, ItemStack stack) {
        return expression.matches(SortRuleExpressionContext.forStack(stack));
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
