package net.kyrptonaught.inventorysorter.sort.expression;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SortRuleExpressionsTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void itemExpressionMatchesByItemIdentifier() {
        SortRuleExpression expression = new ItemExpression(Identifier.parse("minecraft:diamond"));

        Assertions.assertTrue(matches(expression, stack(Items.DIAMOND)));
        Assertions.assertFalse(matches(expression, stack(Items.APPLE)));
    }

    @Test
    void tagExpressionMatchesTagReloadedIntoItemRegistry() {
        TagKey<Item> shulkerBoxes = TagKey.create(Registries.ITEM, Identifier.parse("minecraft:shulker_boxes"));
        SortRuleExpression expression = new TagExpression(shulkerBoxes);

        Assertions.assertFalse(matches(expression, stack(Items.DYED_SHULKER_BOX.white())));

        try {
            applyItemTags(Map.of(shulkerBoxes, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.DYED_SHULKER_BOX.white()))));

            Assertions.assertTrue(matches(expression, stack(Items.DYED_SHULKER_BOX.white())));
            Assertions.assertFalse(matches(expression, stack(Items.APPLE)));
        } finally {
            applyItemTags(Map.of());
        }
    }

    @Test
    void tagExpressionReturnsFalseWhenMinecraftTagsAreNotBound() throws ReflectiveOperationException {
        TagKey<Item> shulkerBoxes = TagKey.create(Registries.ITEM, Identifier.parse("minecraft:shulker_boxes"));
        SortRuleExpression expression = new TagExpression(shulkerBoxes);
        Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(Items.DYED_SHULKER_BOX.white());
        Field tagsField = tagsField(holder);
        Object boundTags = tagsField.get(holder);

        try {
            tagsField.set(holder, null);

            Assertions.assertFalse(matches(expression, stack(Items.DYED_SHULKER_BOX.white())));
        } finally {
            tagsField.set(holder, boundTags);
        }
    }

    @Test
    void componentExpressionMatchesPresentDataComponent() {
        SortRuleExpression expression = new ComponentExpression(DataComponents.BUNDLE_CONTENTS);

        Assertions.assertTrue(matches(expression, bundle()));
        Assertions.assertFalse(matches(expression, stack(Items.APPLE)));
    }

    @Test
    void nameExpressionMatchesDisplayNamePattern() {
        SortRuleExpression expression = new NameExpression(Pattern.compile("^Meza's .*$", Pattern.CASE_INSENSITIVE));

        Assertions.assertTrue(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Meza's Pickaxe")));
        Assertions.assertFalse(matches(expression, namedStack(Items.DIAMOND_PICKAXE, "Fast Pickaxe")));
    }

    @Test
    void notExpressionInvertsChildExpression() {
        SortRuleExpression expression = new NotExpression(new ItemExpression(Identifier.parse("minecraft:apple")));

        Assertions.assertFalse(matches(expression, stack(Items.APPLE)));
        Assertions.assertTrue(matches(expression, stack(Items.DIAMOND)));
    }

    @Test
    void andExpressionIsFalseWhenLeftDoesNotMatch() {
        SortRuleExpression expression = new AndExpression(
                new ItemExpression(Identifier.parse("minecraft:apple")),
                new ItemExpression(Identifier.parse("minecraft:diamond"))
        );

        Assertions.assertFalse(matches(expression, stack(Items.DIAMOND)));
    }

    @Test
    void andExpressionRequiresBothChildrenToMatch() {
        SortRuleExpression expression = new AndExpression(
                new ItemExpression(Identifier.parse("minecraft:diamond")),
                new NameExpression(Pattern.compile("^Diamond$"))
        );

        Assertions.assertTrue(matches(expression, namedStack(Items.DIAMOND, "Diamond")));
        Assertions.assertFalse(matches(expression, namedStack(Items.DIAMOND, "Not Diamond")));
    }

    @Test
    void orExpressionIsTrueWhenLeftMatches() {
        SortRuleExpression expression = new OrExpression(
                new ItemExpression(Identifier.parse("minecraft:apple")),
                new ItemExpression(Identifier.parse("minecraft:diamond"))
        );

        Assertions.assertTrue(matches(expression, stack(Items.APPLE)));
    }

    @Test
    void orExpressionMatchesWhenEitherChildMatches() {
        SortRuleExpression expression = new OrExpression(
                new ItemExpression(Identifier.parse("minecraft:apple")),
                new ItemExpression(Identifier.parse("minecraft:diamond"))
        );

        Assertions.assertTrue(matches(expression, stack(Items.APPLE)));
        Assertions.assertTrue(matches(expression, stack(Items.DIAMOND)));
        Assertions.assertFalse(matches(expression, stack(Items.FEATHER)));
    }

    private static boolean matches(SortRuleExpression expression, ItemStack stack) {
        return expression.matches(SortRuleExpressionContext.forStack(stack));
    }

    private static void applyItemTags(Map<TagKey<Item>, List<Holder<Item>>> tags) {
        try {
            BuiltInRegistries.ITEM.prepareTagReload(new TagLoader.LoadResult<>(Registries.ITEM, tags)).apply();
        } catch (IllegalStateException e) {
            @SuppressWarnings("unchecked")
            WritableRegistry<Item> itemRegistry = (WritableRegistry<Item>) BuiltInRegistries.ITEM;
            @SuppressWarnings("unchecked")
            MappedRegistry<Item> mappedItemRegistry = (MappedRegistry<Item>) BuiltInRegistries.ITEM;
            mappedItemRegistry.bindAllTagsToEmpty();
            itemRegistry.bindTags(tags);
            BuiltInRegistries.ITEM.freeze();
        }
    }

    private static Field tagsField(Holder<Item> holder) throws NoSuchFieldException {
        Field field = holder.getClass().getDeclaredField("tags");
        field.setAccessible(true);
        return field;
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
