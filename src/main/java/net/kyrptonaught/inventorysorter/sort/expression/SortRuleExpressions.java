package net.kyrptonaught.inventorysorter.sort.expression;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.regex.Pattern;

record ItemExpression(Identifier itemId) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return context.itemIdEquals(itemId);
    }
}

record TagExpression(TagKey<Item> tag) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return context.hasTag(tag);
    }
}

record ComponentExpression(DataComponentType<?> componentType) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return context.hasComponent(componentType);
    }
}

record NameExpression(Pattern displayNamePattern) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return displayNamePattern.matcher(context.displayName()).matches();
    }
}

record NotExpression(SortRuleExpression expression) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return !expression.matches(context);
    }
}

record AndExpression(SortRuleExpression left, SortRuleExpression right) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return left.matches(context) && right.matches(context);
    }
}

record OrExpression(SortRuleExpression left, SortRuleExpression right) implements SortRuleExpression {
    @Override
    public boolean matches(SortRuleExpressionContext context) {
        return left.matches(context) || right.matches(context);
    }
}
