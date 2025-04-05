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

public class DoubleClickSortCommand {
    private static final Text FEEDBACK_MESSAGE = Text.translatable("key.inventorysorter.cmd.doubleClickSort");
    private static final String ON_MESSAGE = "On";
    private static final String OFF_MESSAGE = "Off";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {

        dispatcher.register(rootCommand
                .then(CommandManager.literal("doubleClickSort")
                        .requires(CommandPermission.require("doubleclicksort", 0))
                        .then(CommandManager.literal(ON_MESSAGE)
                                .executes(DoubleClickSortCommand::turnOn)
                        )
                        .then(CommandManager.literal(OFF_MESSAGE)
                                .executes(DoubleClickSortCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(() -> Text.translatable("key.inventorysorter.cmd.player-required"), false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withDoubleClick(false);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> Text.of(FEEDBACK_MESSAGE.copy().append(OFF_MESSAGE).getString()), false);
        return 1;
    }

    public static int turnOn(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(() -> Text.translatable("key.inventorysorter.cmd.player-required"), false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withDoubleClick(true);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> Text.of(FEEDBACK_MESSAGE.copy().append(ON_MESSAGE).getString()), false);
        return 1;
    }
}
