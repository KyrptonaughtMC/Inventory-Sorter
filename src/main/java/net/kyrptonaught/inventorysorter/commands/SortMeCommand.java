package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.compat.ServerComponent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SortMeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        dispatcher.register(rootCommand.then(
                Commands.literal("sortme")
                        .requires(CommandPermission.require(CommandRegistry.SORT_ME, 0))
                        .executes(SortMeCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        SortSettings settings = PlatformServices.PLAYER_DATA.getSortSettings(player);
        ServerInventorySorter.sort(player, SortTarget.PLAYER_INVENTORY, settings);

        commandContext.getSource().sendSuccess(() -> ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.sort.sorted"), false);
        return 1;
    }
}
