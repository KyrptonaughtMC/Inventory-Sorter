package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortHighlightedInventoryCommand {
    private static final String SET_KEY = "inventorysorter.cmd.sortHovered.set";
    private static final String GET_KEY = "inventorysorter.cmd.sortHovered.get";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {

        dispatcher.register(rootCommand
                .then(CommandManager.literal("sortHighlightedInventory")
                        .requires(CommandPermission.require("sorthighlightedinventory", 0))
                        .executes(SortHighlightedInventoryCommand::showState)
                        .then(CommandManager.literal("On")
                                .executes(SortHighlightedInventoryCommand::turnOn)
                        )
                        .then(CommandManager.literal("Off")
                                .executes(SortHighlightedInventoryCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortHighlightedInventory(false);
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

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortHighlightedInventory(true);
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

        commandContext.getSource().sendFeedback(() -> CommandTranslations.getFeedbackMessageForState(GET_KEY, settings.sortHighlightedItem()), false);
        return 1;
    }
}
