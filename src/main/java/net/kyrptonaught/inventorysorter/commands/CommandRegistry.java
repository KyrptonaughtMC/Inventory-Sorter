package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandRegistry {
    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext commandRegistryAccess,
            Commands.CommandSelection registrationEnvironment
    ) {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("invsort");

        SortCommand.register(dispatcher, rootCommand);
        DoubleClickSortCommand.register(dispatcher, rootCommand);
        SortPlayerInventoryCommand.register(dispatcher, rootCommand);
        SortHighlightedInventoryCommand.register(dispatcher, rootCommand);
        SortMeCommand.register(dispatcher, rootCommand);
        SortTypeCommand.register(dispatcher, rootCommand);
        NoSortCommand.register(dispatcher, rootCommand);
        ReloadCommand.register(dispatcher, rootCommand);
        ScreenIDCommand.register(dispatcher, rootCommand);

        if(registrationEnvironment.includeDedicated) {
            AdminCommands.register(dispatcher, rootCommand);
        }
    }
}
