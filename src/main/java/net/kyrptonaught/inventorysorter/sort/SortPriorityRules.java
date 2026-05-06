package net.kyrptonaught.inventorysorter.sort;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpressionParser;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class SortPriorityRules {
    public static final SortPriorityRules EMPTY = new SortPriorityRules(SortPriorityRuleChain.EMPTY);

    private static final SortPriorityDecision.PriorityKey DEFAULT_PRIORITY =
            new SortPriorityDecision(SortPriorityPosition.DEFAULT, Integer.MAX_VALUE).priorityKey();

    private final SortPriorityRuleChain ruleChain;

    private SortPriorityRules(SortPriorityRuleChain ruleChain) {
        this.ruleChain = ruleChain;
    }

    public static SortPriorityRules compile(List<SortPriorityRuleSetting> rules) {
        SortPriorityRuleChain ruleChain = SortPriorityRuleChain.compile(rules);
        return ruleChain.isEmpty() ? EMPTY : new SortPriorityRules(ruleChain);
    }

    public static Optional<String> validationError(String expression) {
        return SortRuleExpressionParser.validationError(expression);
    }

    public Comparator<ItemStack> applyTo(Comparator<ItemStack> comparator) {
        if (ruleChain.isEmpty()) {
            return comparator;
        }
        return Comparator.comparing(this::priorityKey).thenComparing(comparator);
    }

    public boolean shouldIgnore(ItemStack stack) {
        return ruleChain.shouldIgnore(stack);
    }

    private SortPriorityDecision.PriorityKey priorityKey(ItemStack stack) {
        return ruleChain.firstPriorityDecision(stack)
                .map(SortPriorityDecision::priorityKey)
                .orElse(DEFAULT_PRIORITY);
    }
}
