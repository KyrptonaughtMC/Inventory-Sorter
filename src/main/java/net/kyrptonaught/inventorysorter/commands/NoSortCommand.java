package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.PLAYER_SORT_PREVENTION;

public class NoSortCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        LiteralArgumentBuilder<CommandSourceStack> nosort = Commands.literal("nosort")
                .requires(CommandPermission.require("nosort", 0));

        dispatcher.register(rootCommand.then(nosort.then(
                Commands.literal("add").executes(NoSortCommand::add)
        )));
        dispatcher.register(rootCommand.then(nosort.then(
                Commands.literal("remove").executes(NoSortCommand::remove)
        )));
        dispatcher.register(rootCommand.then(nosort.then(
                Commands.literal("list").executes(NoSortCommand::list)
        )));

    }

    public static int add(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            playerSortPrevention.preventSortForScreens().add(context.screenId().toString());
            player.setAttached(PLAYER_SORT_PREVENTION, playerSortPrevention);
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.add.fail"), false);
            return 0;
        }

        playerSortPrevention.sync(player);

        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.add.success"), false);
        return 1;
    }

    public static int remove(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            playerSortPrevention.preventSortForScreens().remove(context.screenId().toString());
            player.setAttached(PLAYER_SORT_PREVENTION, playerSortPrevention);
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.remove.fail"), false);
            return 0;
        }

        playerSortPrevention.sync(player);
        commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.nosort.remove.success"), false);
        return 1;
    }

    public static int list(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        commandContext.getSource().sendSuccess(() -> Component.translatable(
                "inventorysorter.cmd.nosort.list",
                String.join(",", playerSortPrevention.preventSortForScreens())
        ), false);

        return 1;
    }
}
