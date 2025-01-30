package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandRegistry {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> rootCommand = CommandManager.literal("invsort");

        SortCommand.register(dispatcher, rootCommand);
        MiddleClickSortCommand.register(dispatcher, rootCommand);
        DoubleClickSortCommand.register(dispatcher, rootCommand);
        SortMeCommand.register(dispatcher, rootCommand);
        SortTypeCommand.register(dispatcher, rootCommand);
        DownloadCompatibilityListCommand.register(dispatcher, rootCommand);
        CompatibilityCommands.register(dispatcher, rootCommand);
    }
}
