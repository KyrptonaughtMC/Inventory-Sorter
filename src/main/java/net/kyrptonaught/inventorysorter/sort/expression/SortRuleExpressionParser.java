package net.kyrptonaught.inventorysorter.sort.expression;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parses the sort-priority rule language into executable Interpreter expressions.
 *
 * Invalid rule text is reported as data rather than thrown through the public API because config
 * loading and runtime sorting both need to tolerate bad user-provided rules. Use
 * {@link #validationError(String)} when presenting feedback to users, and {@link #parse(String)}
 * when compiling rules for sorting.
 */
public final class SortRuleExpressionParser {
    private SortRuleExpressionParser() {
    }

    /**
     * Returns an executable expression when the full input is valid.
     *
     * Empty, partial, or malformed input returns {@link Optional#empty()} so runtime sorting can
     * ignore invalid rules without aborting the whole sort.
     */
    public static Optional<SortRuleExpression> parse(String expression) {
        try {
            return Optional.of(new Parser(expression).parse());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the parser error message that should be shown for invalid user input.
     *
     * A valid expression returns {@link Optional#empty()}. The message includes the parser cursor
     * position so config screens and commands can explain where validation failed.
     */
    public static Optional<String> validationError(String expression) {
        try {
            new Parser(expression).parse();
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }

    private static final class Parser {
        private final String input;
        private int cursor;

        private Parser(String input) {
            this.input = input == null ? "" : input;
        }

        private SortRuleExpression parse() {
            SortRuleExpression expression = parseOr();
            skipWhitespace();
            if (cursor != input.length()) {
                throw error("Unexpected token");
            }
            return expression;
        }

        private SortRuleExpression parseOr() {
            SortRuleExpression expression = parseAnd();
            while (consume('|')) {
                expression = new OrExpression(expression, parseAnd());
            }
            return expression;
        }

        private SortRuleExpression parseAnd() {
            SortRuleExpression expression = parseUnary();
            while (consume('&')) {
                expression = new AndExpression(expression, parseUnary());
            }
            return expression;
        }

        private SortRuleExpression parseUnary() {
            if (consume('!')) {
                return new NotExpression(parseUnary());
            }
            if (consume('(')) {
                SortRuleExpression expression = parseOr();
                if (!consume(')')) {
                    throw error("Expected ')'");
                }
                return expression;
            }
            return parseAtom();
        }

        private SortRuleExpression parseAtom() {
            skipWhitespace();
            if (cursor >= input.length()) {
                throw error("Expected a rule atom");
            }

            if (input.regionMatches(true, cursor, "name:", 0, "name:".length())) {
                cursor += "name:".length();
                return new NameExpression(parseQuotedGlobPattern());
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

        private Pattern parseQuotedGlobPattern() {
            return Pattern.compile(toAnchoredRegex(readQuotedGlobTokens()), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }

        private List<GlobToken> readQuotedGlobTokens() {
            skipWhitespace();
            if (cursor >= input.length() || input.charAt(cursor) != '"') {
                throw error("Expected quoted name glob");
            }
            cursor++;

            List<GlobToken> tokens = new ArrayList<>();
            while (cursor < input.length()) {
                char value = input.charAt(cursor++);
                if (value == '"') {
                    return tokens;
                }
                if (value == '\\') {
                    tokens.add(GlobToken.literal(readEscapedGlobCharacter()));
                    continue;
                }
                tokens.add(value == '*' ? GlobToken.anyText() : GlobToken.literal(value));
            }

            throw error("Expected closing quote");
        }

        private char readEscapedGlobCharacter() {
            if (cursor >= input.length()) {
                throw error("Expected escaped character");
            }
            return input.charAt(cursor++);
        }

        private String toAnchoredRegex(List<GlobToken> glob) {
            StringBuilder regex = new StringBuilder("^");
            for (GlobToken token : glob) {
                appendGlobToken(regex, token);
            }
            regex.append("$");
            return regex.toString();
        }

        private void appendGlobToken(StringBuilder regex, GlobToken token) {
            if (token.wildcard()) {
                regex.append(".*");
                return;
            }
            regex.append(Pattern.quote(String.valueOf(token.value())));
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

        private record GlobToken(char value, boolean wildcard) {
            private static GlobToken literal(char value) {
                return new GlobToken(value, false);
            }

            private static GlobToken anyText() {
                return new GlobToken('*', true);
            }
        }
    }
}
