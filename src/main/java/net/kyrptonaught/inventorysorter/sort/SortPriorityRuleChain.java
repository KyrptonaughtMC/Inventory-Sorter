package net.kyrptonaught.inventorysorter.sort;

import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpression;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpressionContext;
import net.kyrptonaught.inventorysorter.sort.expression.SortRuleExpressionParser;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ordered chain of compiled sort-priority rules.
 *
 * Rule handlers are evaluated in config order. Ignore decisions are queried separately because
 * ignoring removes a stack from the sortable set before normal priority ordering is considered.
 * Priority decisions still use first-match-wins semantics among non-ignore rules.
 */
public final class SortPriorityRuleChain {
    public static final SortPriorityRuleChain EMPTY = new SortPriorityRuleChain(List.of());

    private final List<RuleHandler> handlers;

    private SortPriorityRuleChain(List<RuleHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public static SortPriorityRuleChain compile(List<SortPriorityRuleSetting> rules) {
        List<RuleHandler> handlers = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            Optional<SortRuleExpression> expression = SortRuleExpressionParser.parse(rules.get(i).match());
            if (expression.isPresent()) {
                handlers.add(new RuleHandler(expression.get(), rules.get(i).position(), i));
            }
        }
        return handlers.isEmpty() ? EMPTY : new SortPriorityRuleChain(handlers);
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    public boolean shouldIgnore(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (RuleHandler handler : handlers) {
            if (handler.handle(stack).filter(SortPriorityDecision::ignoresStack).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public Optional<SortPriorityDecision> firstPriorityDecision(ItemStack stack) {
        for (RuleHandler handler : handlers) {
            Optional<SortPriorityDecision> decision = handler.handle(stack);
            if (decision.isPresent() && !decision.get().ignoresStack()) {
                return decision;
            }
        }
        return Optional.empty();
    }

    private record RuleHandler(SortRuleExpression expression, SortPriorityPosition position, int order) {
        private Optional<SortPriorityDecision> handle(ItemStack stack) {
            if (expression.matches(SortRuleExpressionContext.forStack(stack))) {
                return Optional.of(new SortPriorityDecision(position, order));
            }
            return Optional.empty();
        }
    }
}
