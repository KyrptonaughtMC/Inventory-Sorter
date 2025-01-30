package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
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
        if (commandContext.getSource().getPlayer() == null) return 0;
        ((InvSorterPlayer) commandContext.getSource().getPlayer()).setMiddleClick(false);
        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> FEEDBACK_MESSAGE.copy().append(OFF_MESSAGE), false);
        return 1;
    }

    public static int turnOn(CommandContext<ServerCommandSource> commandContext) {
        if (commandContext.getSource().getPlayer() == null) return 0;
        ((InvSorterPlayer) commandContext.getSource().getPlayer()).setMiddleClick(true);
        // @TODO instead of appending, use proper translation with placeholders
        commandContext.getSource().sendFeedback(() -> FEEDBACK_MESSAGE.copy().append(ON_MESSAGE), false);
        return 1;
    }
}
