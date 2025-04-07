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

public class SortCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("sort")
                .requires(CommandPermission.require("sort", 0))
                .executes(SortCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS);

        Text feedBack = InventoryHelper.sortTargetedBlock(player, settings.sortType());
        commandContext.getSource().sendFeedback(() -> Text.of(feedBack.getString()), false);
        return 1;
    }
}
