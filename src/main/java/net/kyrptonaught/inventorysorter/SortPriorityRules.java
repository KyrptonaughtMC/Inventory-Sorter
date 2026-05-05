package net.kyrptonaught.inventorysorter;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class SortPriorityRules {
    public static final SortPriorityRules EMPTY = new SortPriorityRules(List.of());

    private final List<CompiledRule> rules;

    private SortPriorityRules(List<CompiledRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static SortPriorityRules compile(List<SortPriorityRule> rules) {
        List<CompiledRule> compiledRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            Optional<Predicate<ItemStack>> matcher = SortPriorityRuleExpression.parse(rules.get(i).match());
            if (matcher.isPresent()) {
                compiledRules.add(new CompiledRule(matcher.get(), rules.get(i).position(), i));
            }
        }
        return compiledRules.isEmpty() ? EMPTY : new SortPriorityRules(compiledRules);
    }

    public static Optional<String> validationError(String expression) {
        return SortPriorityRuleExpression.validationError(expression);
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

    private record CompiledRule(Predicate<ItemStack> matcher, SortPriorityPosition position, int order) {
        private boolean matches(ItemStack stack) {
            return matcher.test(stack);
        }
    }

    private sealed interface Expression permits ItemExpression, TagExpression, ComponentExpression, NotExpression, AndExpression, OrExpression {
        boolean matches(ItemStack stack);
    }

    private record ItemExpression(Identifier itemId) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
        }
    }

    private record TagExpression(TagKey<Item> tag) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            try {
                return BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).is(tag);
            } catch (IllegalStateException e) {
                return false;
            }
        }
    }

    private record ComponentExpression(DataComponentType<?> componentType) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            return stack.has(componentType);
        }
    }

    private record NotExpression(Expression expression) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            return !expression.matches(stack);
        }
    }

    private record AndExpression(Expression left, Expression right) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            return left.matches(stack) && right.matches(stack);
        }
    }

    private record OrExpression(Expression left, Expression right) implements Expression {
        @Override
        public boolean matches(ItemStack stack) {
            return left.matches(stack) || right.matches(stack);
        }
    }

    private static final class SortPriorityRuleExpression {
        static Optional<Predicate<ItemStack>> parse(String expression) {
            try {
                return Optional.of(new Parser(expression).parse()::matches);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        static Optional<String> validationError(String expression) {
            try {
                new Parser(expression).parse();
                return Optional.empty();
            } catch (IllegalArgumentException e) {
                return Optional.of(e.getMessage());
            }
        }
    }

    private static final class Parser {
        private final String input;
        private int cursor;

        private Parser(String input) {
            this.input = input == null ? "" : input;
        }

        private Expression parse() {
            Expression expression = parseOr();
            skipWhitespace();
            if (cursor != input.length()) {
                throw error("Unexpected token");
            }
            return expression;
        }

        private Expression parseOr() {
            Expression expression = parseAnd();
            while (consume('|')) {
                expression = new OrExpression(expression, parseAnd());
            }
            return expression;
        }

        private Expression parseAnd() {
            Expression expression = parseUnary();
            while (consume('&')) {
                expression = new AndExpression(expression, parseUnary());
            }
            return expression;
        }

        private Expression parseUnary() {
            if (consume('!')) {
                return new NotExpression(parseUnary());
            }
            if (consume('(')) {
                Expression expression = parseOr();
                if (!consume(')')) {
                    throw error("Expected ')'");
                }
                return expression;
            }
            return parseAtom();
        }

        private Expression parseAtom() {
            skipWhitespace();
            if (cursor >= input.length()) {
                throw error("Expected a rule atom");
            }

            char prefix = input.charAt(cursor);
            if (prefix == '#') {
                cursor++;
                Identifier id = parseIdentifier();
                return new TagExpression(TagKey.create(Registries.ITEM, id));
            }
            if (prefix == '@') {
                cursor++;
                Identifier id = parseIdentifier();
                DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(id)
                        .orElseThrow(() -> error("Unknown data component: " + id));
                return new ComponentExpression(componentType);
            }
            return new ItemExpression(parseIdentifier());
        }

        private Identifier parseIdentifier() {
            skipWhitespace();
            int start = cursor;
            while (cursor < input.length() && isIdentifierChar(input.charAt(cursor))) {
                cursor++;
            }
            if (start == cursor) {
                throw error("Expected identifier");
            }
            String raw = input.substring(start, cursor);
            Identifier id = Identifier.tryParse(raw.contains(":") ? raw : "minecraft:" + raw);
            if (id == null) {
                throw error("Invalid identifier: " + raw);
            }
            return id;
        }

        private boolean consume(char token) {
            skipWhitespace();
            if (cursor < input.length() && input.charAt(cursor) == token) {
                cursor++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (cursor < input.length() && Character.isWhitespace(input.charAt(cursor))) {
                cursor++;
            }
        }

        private boolean isIdentifierChar(char value) {
            return Character.isLetterOrDigit(value) || value == '_' || value == '-' || value == '.' || value == '/' || value == ':';
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at position " + cursor);
        }
    }
}
