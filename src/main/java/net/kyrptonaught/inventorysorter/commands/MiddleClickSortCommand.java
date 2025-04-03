package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MiddleClickSortCommand {
    private static final Text FEEDBACK_MESSAGE = Text.translatable("key.inventorysorter.cmd.middleClickSort");
    private static final String ON_MESSAGE = "On";
    private static final String OFF_MESSAGE = "Off";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {

        dispatcher.register(rootCommand
                .then(CommandManager.literal("middleClickSort")
                        .then(CommandManager.literal(ON_MESSAGE)
                                .executes(MiddleClickSortCommand::turnOn)
                        )
                        .then(CommandManager.literal(OFF_MESSAGE)
                                .executes(MiddleClickSortCommand::turnOff)
                        )));
    }

    public static int turnOff(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        InvSorterPlayer sortPlayer = (InvSorterPlayer) player;
        if (player == null) return 0;
        sortPlayer.setMiddleClick(false);
        sortPlayer.syncSettings(player);
        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> Text.of(FEEDBACK_MESSAGE.copy().append(OFF_MESSAGE).getString()), false);
        return 1;
    }

    public static int turnOn(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        InvSorterPlayer sortPlayer = (InvSorterPlayer) player;

        if (player == null) return 0;
        sortPlayer.setMiddleClick(true);
        sortPlayer.syncSettings(player);
        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> Text.of(FEEDBACK_MESSAGE.copy().append(ON_MESSAGE).getString()), false);
        return 1;
    }
}
