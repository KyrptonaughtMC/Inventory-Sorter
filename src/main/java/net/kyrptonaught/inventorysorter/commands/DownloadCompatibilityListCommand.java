package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.compat.sources.CommandLoader;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public class DownloadCompatibilityListCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("downloadCompatibilityList")
                .then(CommandManager.argument("URL", StringArgumentType.greedyString())
                        .executes(DownloadCompatibilityListCommand::run))));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        String URL = StringArgumentType.getString(commandContext, "URL");
        CommandLoader remoteConfigData = new CommandLoader(URL);

        remoteConfigData.getPreventSort().forEach(id -> getConfig().preventSortForScreens.add(id.toString()));
        remoteConfigData.getShouldHideSortButtons().forEach(id -> getConfig().hideButtonsForScreens.add(id.toString()));

        getConfig().save();
        compatibility.reload();

        commandContext.getSource().getServer().getPlayerManager().getPlayerList().forEach(SyncBlacklistPacket::sync);
        return 1;
    }
}
