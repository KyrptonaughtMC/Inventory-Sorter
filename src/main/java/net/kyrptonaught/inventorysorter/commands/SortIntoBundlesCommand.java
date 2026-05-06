package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SortIntoBundlesCommand {
    private static final String SET_KEY = "inventorysorter.cmd.sortIntoBundles.set";
    private static final String GET_KEY = "inventorysorter.cmd.sortIntoBundles.get";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {

        dispatcher.register(rootCommand
                .then(Commands.literal("sortIntoBundles")
                        .requires(CommandPermission.require("sortintobundles", 0))
                        .executes(SortIntoBundlesCommand::showState)
                        .then(Commands.literal("on")
                                .executes(SortIntoBundlesCommand::turnOn)
                        )
                        .then(Commands.literal("off")
                                .executes(SortIntoBundlesCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player).withSortIntoBundles(false);
        PlatformServices.PLAYER_DATA.setSortSettings(player, settings);

        settings.sync(player);

        commandContext.getSource().sendSuccess(() -> CommandTranslations.getOffMessage(SET_KEY), false);
        return 1;
    }

    public static int turnOn(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player).withSortIntoBundles(true);
        PlatformServices.PLAYER_DATA.setSortSettings(player, settings);

        settings.sync(player);

        commandContext.getSource().sendSuccess(() -> CommandTranslations.getOnMessage(SET_KEY), false);
        return 1;
    }

    public static int showState(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player);

        commandContext.getSource().sendSuccess(() -> CommandTranslations.getFeedbackMessageForState(GET_KEY, settings.sortIntoBundles()), false);
        return 1;
    }
}
