package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpression;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpressionContext;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpressionParser;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class SortPriorityRules {
    public static final SortPriorityRules EMPTY = new SortPriorityRules(List.of());

    private final List<CompiledRule> rules;

    private SortPriorityRules(List<CompiledRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static SortPriorityRules compile(List<SortPriorityRule> rules) {
        List<CompiledRule> compiledRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            Optional<SortRuleExpression> expression = SortRuleExpressionParser.parse(rules.get(i).match());
            if (expression.isPresent()) {
                compiledRules.add(new CompiledRule(expression.get(), rules.get(i).position(), i));
            }
        }
        return compiledRules.isEmpty() ? EMPTY : new SortPriorityRules(compiledRules);
    }

    public static Optional<String> validationError(String expression) {
        return SortRuleExpressionParser.validationError(expression);
    }

    public Comparator<ItemStack> applyTo(Comparator<ItemStack> comparator) {
        if (rules.isEmpty()) {
            return comparator;
        }
        return Comparator.comparing(this::priority).thenComparing(comparator);
    }

    public boolean shouldIgnore(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (CompiledRule rule : rules) {
            if (rule.position() == SortPriorityPosition.IGNORE && rule.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    private CompiledRule firstMatchingPriorityRule(ItemStack stack) {
        for (CompiledRule rule : rules) {
            if (rule.position() == SortPriorityPosition.IGNORE) {
                continue;
            }
            if (rule.matches(stack)) {
                return rule;
            }
        }
        return null;
    }

    private Priority priority(ItemStack stack) {
        CompiledRule rule = firstMatchingPriorityRule(stack);
        if (rule != null) {
            return new Priority(rule.position().sortBucket(), rule.order());
        }
        return new Priority(SortPriorityPosition.DEFAULT.sortBucket(), Integer.MAX_VALUE);
    }

    private record Priority(int bucket, int order) implements Comparable<Priority> {
        @Override
        public int compareTo(Priority other) {
            int bucketComparison = Integer.compare(bucket, other.bucket);
            if (bucketComparison != 0) {
                return bucketComparison;
            }
            return Integer.compare(order, other.order);
        }
    }

    private record CompiledRule(SortRuleExpression expression, SortPriorityPosition position, int order) {
        private boolean matches(ItemStack stack) {
            return expression.matches(SortRuleExpressionContext.forStack(stack));
        }
    }
}
