package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class DoubleClickSortCommand {
    private static final String SET_KEY = "inventorysorter.cmd.doubleClickSort.set";
    private static final String GET_KEY = "inventorysorter.cmd.doubleClickSort.get";
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {

        dispatcher.register(rootCommand
                .then(CommandManager.literal("doubleClickSort")
                        .requires(CommandPermission.require("doubleclicksort", 0))
                        .executes(DoubleClickSortCommand::showState)
                        .then(CommandManager.literal("on")
                                .executes(DoubleClickSortCommand::turnOn)
                        )
                        .then(CommandManager.literal("off")
                                .executes(DoubleClickSortCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withDoubleClick(false);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        commandContext.getSource().sendFeedback(() -> CommandTranslations.getOffMessage(SET_KEY), false);
        return 1;
    }

    public static int turnOn(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withDoubleClick(true);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        commandContext.getSource().sendFeedback(() -> CommandTranslations.getOnMessage(SET_KEY), false);
        return 1;
    }

    public static int showState(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);

        commandContext.getSource().sendFeedback(() -> CommandTranslations.getFeedbackMessageForState(GET_KEY, settings.enableDoubleClick()), false);
        return 1;
    }
}
