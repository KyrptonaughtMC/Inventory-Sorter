package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.ReloadConfigPacket;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ReloadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("reload")
                .requires(CommandPermission.require("reload", 0))
                .executes(ReloadCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        new ReloadConfigPacket().fire(player);
        commandContext.getSource().sendFeedback(() -> Text.of("Configuration reloaded"), false);
        return 1;
    }
}
