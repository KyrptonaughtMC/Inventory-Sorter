package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortTypeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        for (SortType sortType : SortType.values()) {
            dispatcher.register(rootCommand
                    .then(CommandManager.literal("sortType")
                            .requires(CommandPermission.require("sorttype", 0))
                            .then(CommandManager.literal(sortType.name())
                                    .executes(context -> SortTypeCommand.run(context, sortType))))
            );
        }
    }

    public static int run(CommandContext<ServerCommandSource> commandContext, SortType sortType) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortType(sortType);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.sorttype.success", Text.translatable(sortType.getTranslationKey())), false);
        return 1;
    }
}
