package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.compatibility;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.getConfig;

public class CompatibilityCommands {

    private static final String SCREENID = "screenid";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {


        dispatcher.register(rootCommand
                .then(CommandManager.literal("preventSort")
                        .requires((source) -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument(SCREENID, StringArgumentType.greedyString())
                                .executes(context -> run(context, getConfig().preventSortForScreens)))));


        dispatcher.register(rootCommand
                .then(CommandManager.literal("hideButton")
                        .requires((source) -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument(SCREENID, StringArgumentType.greedyString())
                                .executes(context -> run(context, getConfig().hideButtonsForScreens)))));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext, List<String> list) {
        String id = StringArgumentType.getString(commandContext, SCREENID);
        if (Registries.SCREEN_HANDLER.containsId(Identifier.of(id))) {
            list.add(id);
            getConfig().save();
            compatibility.reload();
            commandContext.getSource().getServer().getPlayerManager().getPlayerList().forEach(SyncBlacklistPacket::sync);
            commandContext.getSource().sendFeedback(() -> Text.translatable("key.inventorysorter.cmd.addblacklist").append(id), false);
        } else
            commandContext.getSource().sendFeedback(() -> Text.translatable("key.inventorysorter.cmd.invalidscreen"), false);
        return 1;
    }
}
