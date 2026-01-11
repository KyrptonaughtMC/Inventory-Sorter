package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortPlayerInventoryCommand {
    private static final String SET_KEY = "inventorysorter.cmd.sortPlayerInventory.set";
    private static final String GET_KEY = "inventorysorter.cmd.sortPlayerInventory.get";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {

        dispatcher.register(rootCommand
                .then(Commands.literal("sortPlayerInventory")
                        .requires(CommandPermission.require("sortplayerinventory", 0))
                        .executes(SortPlayerInventoryCommand::showState)
                        .then(Commands.literal("on")
                                .executes(SortPlayerInventoryCommand::turnOn)
                        )
                        .then(Commands.literal("off")
                                .executes(SortPlayerInventoryCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortPlayerInventory(false);
        player.setAttached(SORT_SETTINGS, settings);

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

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortPlayerInventory(true);
        player.setAttached(SORT_SETTINGS, settings);

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

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);

        commandContext.getSource().sendSuccess(() -> CommandTranslations.getFeedbackMessageForState(GET_KEY, settings.sortPlayerInventory()), false);
        return 1;
    }
}
