package net.kyrptonaught.inventorysorter.sort.expression;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Evaluation context for sort-priority rule expressions.
 *
 * The context owns Minecraft lookups needed by expression nodes. This keeps expression nodes
 * focused on the Interpreter tree instead of on registry access details.
 */
public final class SortRuleExpressionContext {
    private final ItemStack stack;

    private SortRuleExpressionContext(ItemStack stack) {
        this.stack = stack;
    }

    public static SortRuleExpressionContext forStack(ItemStack stack) {
        return new SortRuleExpressionContext(stack);
    }

    public boolean itemIdEquals(Identifier itemId) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
    }

    public boolean hasTag(TagKey<Item> tag) {
        try {
            return BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).is(tag);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public boolean hasComponent(DataComponentType<?> componentType) {
        return stack.has(componentType);
    }

    public String displayName() {
        return stack.getHoverName().getString();
    }

}
