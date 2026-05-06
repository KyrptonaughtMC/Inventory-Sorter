package net.kyrptonaught.inventorysorter.sort.expression;

/**
 * Executable predicate produced from the sort-priority rule language.
 *
 * Expressions only answer whether a context matches the parsed rule text. They do not know about
 * rule positions, priority buckets, inventory layout, stack merging, or how a layout is applied.
 */
public sealed interface SortRuleExpression permits ItemExpression, TagExpression, ComponentExpression, NameExpression, NotExpression, AndExpression, OrExpression {
    boolean matches(SortRuleExpressionContext context);
}
