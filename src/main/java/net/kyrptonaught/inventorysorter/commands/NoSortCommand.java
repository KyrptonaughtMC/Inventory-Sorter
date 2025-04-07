package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.PLAYER_SORT_PREVENTION;

public class NoSortCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        LiteralArgumentBuilder<ServerCommandSource> nosort = CommandManager.literal("nosort")
                .requires(CommandPermission.require("nosort", 0));

        dispatcher.register(rootCommand.then(nosort.then(
                CommandManager.literal("add").executes(NoSortCommand::add)
        )));
        dispatcher.register(rootCommand.then(nosort.then(
                CommandManager.literal("remove").executes(NoSortCommand::remove)
        )));
        dispatcher.register(rootCommand.then(nosort.then(
                CommandManager.literal("list").executes(NoSortCommand::list)
        )));

    }

    public static int add(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            playerSortPrevention.preventSortForScreens().add(context.screenId().toString());
            player.setAttached(PLAYER_SORT_PREVENTION, playerSortPrevention);
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.nosort.add.fail"), false);
            return 0;
        }

        playerSortPrevention.sync(player);

        commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.nosort.add.success"), false);
        return 1;
    }

    public static int remove(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        Boolean success = InventoryHelper.withTargetedScreenHandler(player, context -> {
            playerSortPrevention.preventSortForScreens().remove(context.screenId().toString());
            player.setAttached(PLAYER_SORT_PREVENTION, playerSortPrevention);
            return true;
        });

        if (Boolean.FALSE.equals(success)) {
            commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.nosort.remove.fail"), false);
            return 0;
        }

        playerSortPrevention.sync(player);
        commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.nosort.remove.success"), false);
        return 1;
    }

    public static int list(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();
        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }
        PlayerSortPrevention playerSortPrevention = player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);

        commandContext.getSource().sendFeedback(() -> Text.translatable(
                "inventorysorter.cmd.nosort.list",
                String.join(",", playerSortPrevention.preventSortForScreens())
        ), false);

        return 1;
    }
}
