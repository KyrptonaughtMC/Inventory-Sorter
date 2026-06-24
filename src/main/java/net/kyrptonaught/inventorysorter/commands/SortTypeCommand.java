package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.compat.ServerComponent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SortTypeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        for (SortType sortType : SortType.values()) {
            dispatcher.register(rootCommand
                    .then(Commands.literal("sortType")
                            .requires(CommandPermission.require(CommandRegistry.SORT_TYPE, 0))
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

        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player).withSortType(sortType);
        PlatformServices.PLAYER_DATA.setSortSettings(player, settings);

        settings.sync(player);

        commandContext.getSource().sendSuccess(() -> ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.sorttype.success", ServerComponent.lang(player.clientInformation().language()).translate(sortType.getTranslationKey())), false);
        return 1;
    }
}
