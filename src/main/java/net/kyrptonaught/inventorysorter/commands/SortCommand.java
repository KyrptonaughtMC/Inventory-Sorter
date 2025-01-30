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

public class SortCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("sort").executes(SortCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        HitResult hit = commandContext.getSource().getPlayer().raycast(6, 1, false);
        if (hit instanceof BlockHitResult) {
            Text feedBack = InventoryHelper.sortBlock(commandContext.getSource().getPlayer().getWorld(), ((BlockHitResult) hit).getBlockPos(), commandContext.getSource().getPlayer(), ((InvSorterPlayer) commandContext.getSource().getPlayer()).getSortType());
            commandContext.getSource().sendFeedback(() -> feedBack, false);
        }
        return 1;
    }
}
