package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.SORT_SETTINGS;

public class SortTypeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        for (SortType sortType : SortType.values()) {
            dispatcher.register(rootCommand
                    .then(Commands.literal("sortType")
                            .requires(CommandPermission.require("sorttype", 0))
                            .then(Commands.literal(sortType.name())
                                    .executes(context -> SortTypeCommand.run(context, sortType))))
            );
        }
    }

    public static int run(CommandContext<CommandSourceStack> commandContext, SortType sortType) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        SortSettings settings = player.getAttachedOrCreate(SORT_SETTINGS).withSortType(sortType);
        player.setAttached(SORT_SETTINGS, settings);

        settings.sync(player);

        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.sorttype.success", Component.translatable(sortType.getTranslationKey())), false);
        return 1;
    }
}
