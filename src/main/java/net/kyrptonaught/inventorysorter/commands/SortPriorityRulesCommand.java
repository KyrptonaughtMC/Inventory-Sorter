package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.kyrptonaught.inventorysorter.SortPriorityRules;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class SortPriorityRulesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        LiteralArgumentBuilder<CommandSourceStack> priority = Commands.literal("priority")
                .requires(CommandPermission.require("priority", 0))
                .then(Commands.literal("list")
                        .executes(SortPriorityRulesCommand::list))
                .then(Commands.literal("remove")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(SortPriorityRulesCommand::remove)))
                .then(Commands.literal("move")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .then(Commands.literal("up")
                                        .executes(context -> move(context, MoveDirection.UP)))
                                .then(Commands.literal("down")
                                        .executes(context -> move(context, MoveDirection.DOWN)))))
                .then(Commands.literal("clear")
                        .executes(SortPriorityRulesCommand::clear));

        for (SortPriorityPosition position : SortPriorityPosition.values()) {
            priority.then(Commands.literal("add")
                    .then(Commands.literal(position.configValue())
                            .then(Commands.argument("match", StringArgumentType.greedyString())
                                    .executes(context -> add(context, position)))));
            priority.then(Commands.literal("set")
                    .then(Commands.argument("index", IntegerArgumentType.integer(1))
                            .then(Commands.literal(position.configValue())
                                    .executes(context -> setPosition(context, position))
                                    .then(Commands.argument("match", StringArgumentType.greedyString())
                                            .executes(context -> set(context, position))))));
        }

        dispatcher.register(rootCommand.then(priority));
    }

    static List<SortPriorityRule> addRule(List<SortPriorityRule> rules, SortPriorityRule rule) {
        List<SortPriorityRule> updatedRules = new ArrayList<>(rules);
        updatedRules.add(rule);
        return updatedRules;
    }

    static List<SortPriorityRule> setRule(List<SortPriorityRule> rules, int userIndex, SortPriorityRule rule) {
        List<SortPriorityRule> updatedRules = new ArrayList<>(rules);
        updatedRules.set(toListIndex(updatedRules, userIndex), rule);
        return updatedRules;
    }

    static List<SortPriorityRule> setRulePosition(List<SortPriorityRule> rules, int userIndex, SortPriorityPosition position) {
        List<SortPriorityRule> updatedRules = new ArrayList<>(rules);
        int index = toListIndex(updatedRules, userIndex);
        SortPriorityRule currentRule = updatedRules.get(index);
        updatedRules.set(index, new SortPriorityRule(currentRule.match(), position));
        return updatedRules;
    }

    static List<SortPriorityRule> removeRule(List<SortPriorityRule> rules, int userIndex) {
        List<SortPriorityRule> updatedRules = new ArrayList<>(rules);
        updatedRules.remove(toListIndex(updatedRules, userIndex));
        return updatedRules;
    }

    static List<SortPriorityRule> moveRule(List<SortPriorityRule> rules, int userIndex, MoveDirection direction) {
        List<SortPriorityRule> updatedRules = new ArrayList<>(rules);
        int index = toListIndex(updatedRules, userIndex);
        int destination = direction.destination(index);
        if (destination < 0 || destination >= updatedRules.size()) {
            throw new IllegalArgumentException("Cannot move rule " + userIndex + " " + direction.commandName);
        }
        SortPriorityRule rule = updatedRules.remove(index);
        updatedRules.add(destination, rule);
        return updatedRules;
    }

    private static int add(CommandContext<CommandSourceStack> context, SortPriorityPosition position) {
        return updateRules(context, rules -> addRule(rules, parseRule(context, position)), "inventorysorter.cmd.priority.add.success");
    }

    private static int set(CommandContext<CommandSourceStack> context, SortPriorityPosition position) {
        int index = IntegerArgumentType.getInteger(context, "index");
        return updateRules(context, rules -> setRule(rules, index, parseRule(context, position)), "inventorysorter.cmd.priority.set.success");
    }

    private static int setPosition(CommandContext<CommandSourceStack> context, SortPriorityPosition position) {
        int index = IntegerArgumentType.getInteger(context, "index");
        return updateRules(context, rules -> setRulePosition(rules, index, position), "inventorysorter.cmd.priority.set.success");
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        return updateRules(context, rules -> removeRule(rules, index), "inventorysorter.cmd.priority.remove.success");
    }

    private static int move(CommandContext<CommandSourceStack> context, MoveDirection direction) {
        int index = IntegerArgumentType.getInteger(context, "index");
        return updateRules(context, rules -> moveRule(rules, index, direction), "inventorysorter.cmd.priority.move.success");
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        return updateRules(context, ignored -> List.of(), "inventorysorter.cmd.priority.clear.success");
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        List<SortPriorityRule> rules = PlatformServices.PLAYER_DATA.getSortSettings(player).sortPriorityRules();
        if (rules.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.priority.list.empty"), false);
            return 1;
        }

        for (int i = 0; i < rules.size(); i++) {
            int userIndex = i + 1;
            SortPriorityRule rule = rules.get(i);
            context.getSource().sendSuccess(() -> Component.translatable(
                    "inventorysorter.cmd.priority.list.entry",
                    userIndex,
                    Component.translatable(rule.position().getTranslationKey()),
                    rule.match()
            ), false);
        }
        return rules.size();
    }

    private static int updateRules(CommandContext<CommandSourceStack> context, RulesUpdate update, String successKey) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        try {
            SortSettings currentSettings = PlatformServices.PLAYER_DATA.getSortSettings(player);
            SortSettings updatedSettings = currentSettings.withSortPriorityRules(update.apply(currentSettings.sortPriorityRules()));
            PlatformServices.PLAYER_DATA.setSortSettings(player, updatedSettings);
            updatedSettings.sync(player);
            context.getSource().sendSuccess(() -> Component.translatable(successKey), false);
            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.translatable("inventorysorter.cmd.priority.error", e.getMessage()));
            return 0;
        }
    }

    private static SortPriorityRule parseRule(CommandContext<CommandSourceStack> context, SortPriorityPosition position) {
        String match = StringArgumentType.getString(context, "match").trim();
        if (match.isBlank()) {
            throw new IllegalArgumentException("Match expression cannot be blank");
        }
        SortPriorityRules.validationError(match).ifPresent(message -> {
            throw new IllegalArgumentException(message);
        });
        return new SortPriorityRule(match, position);
    }

    private static int toListIndex(List<SortPriorityRule> rules, int userIndex) {
        int index = userIndex - 1;
        if (index < 0 || index >= rules.size()) {
            throw new IllegalArgumentException("Unknown rule index: " + userIndex);
        }
        return index;
    }

    @FunctionalInterface
    private interface RulesUpdate {
        List<SortPriorityRule> apply(List<SortPriorityRule> rules);
    }

    enum MoveDirection {
        UP("up", -1),
        DOWN("down", 1);

        private final String commandName;
        private final int offset;

        MoveDirection(String commandName, int offset) {
            this.commandName = commandName;
            this.offset = offset;
        }

        private int destination(int index) {
            return index + offset;
        }
    }
}
