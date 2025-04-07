package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortMeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(
                CommandManager.literal("sortme")
                        .requires(CommandPermission.require("sortme", 0))
                        .executes(SortMeCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);
        InventoryHelper.sortInventory(player, true, settings.sortType());

        commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.sort.sorted"), false);
        return 1;
    }
}
