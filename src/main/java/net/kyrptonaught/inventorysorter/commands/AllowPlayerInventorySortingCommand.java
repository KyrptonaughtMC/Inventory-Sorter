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

public final class AllowPlayerInventorySortingCommand {
    private static final String SET_KEY = "inventorysorter.cmd.allowPlayerInventorySorting.set";
    private static final String GET_KEY = "inventorysorter.cmd.allowPlayerInventorySorting.get";

    private AllowPlayerInventorySortingCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        dispatcher.register(rootCommand.then(Commands.literal("allowPlayerInventorySorting")
                .requires(CommandPermission.require(CommandRegistry.ALLOW_PLAYER_INVENTORY_SORTING, 0))
                .executes(AllowPlayerInventorySortingCommand::showState)
                .then(Commands.literal("on").executes(context -> setAllowed(context, true)))
                .then(Commands.literal("off").executes(context -> setAllowed(context, false)))));
    }

    private static int setAllowed(CommandContext<CommandSourceStack> context, boolean allowed) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player).withAllowPlayerInventorySorting(allowed);
        PlatformServices.PLAYER_DATA.setSortSettings(player, settings);
        settings.sync(player);
        context.getSource().sendSuccess(() -> CommandTranslations.getFeedbackMessageForState(player, SET_KEY, allowed), false);
        return 1;
    }

    private static int showState(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        boolean allowed = PlatformServices.PLAYER_DATA.getSortSettings(player).allowPlayerInventorySorting();
        context.getSource().sendSuccess(() -> CommandTranslations.getFeedbackMessageForState(player, GET_KEY, allowed), false);
        return 1;
    }
}
