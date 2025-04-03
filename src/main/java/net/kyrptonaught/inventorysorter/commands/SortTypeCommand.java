package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortTypeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        for (SortCases.SortType sortType : SortCases.SortType.values()) {
            dispatcher.register(rootCommand
                    .then(CommandManager.literal("sortType")
                            .then(CommandManager.literal(sortType.name())
                                    .executes(context -> SortTypeCommand.run(context, sortType))))
            );
        }
    }

    public static int run(CommandContext<ServerCommandSource> commandContext, SortCases.SortType sortType) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) return 0;

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortType(sortType);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);
        Text feedBack = Text.translatable("key.inventorysorter.cmd.updatesortingtype");
        commandContext.getSource().sendFeedback(() -> Text.of(feedBack.getString()), false);
        return 1;
    }
}
