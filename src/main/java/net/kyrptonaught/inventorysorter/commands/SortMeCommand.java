package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class SortMeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("sortme").executes(SortMeCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        InventoryHelper.sortInventory(commandContext.getSource().getPlayer(), true, ((InvSorterPlayer) commandContext.getSource().getPlayer()).getSortType());

        Text feedBack = Text.translatable("key.inventorysorter.sorting.sorted");
        commandContext.getSource().sendFeedback(() -> feedBack, false);
        return 1;
    }
}
