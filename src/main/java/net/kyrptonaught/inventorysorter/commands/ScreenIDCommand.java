package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.compat.ServerComponent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.screen.TargetedInventoryResolver;
import net.kyrptonaught.inventorysorter.screen.TargetedScreenContext;
import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class ScreenIDCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        dispatcher.register(rootCommand.then(Commands.literal("screenID")
                .requires(CommandPermission.require(CommandRegistry.SCREEN_ID, 0))
                .executes(ScreenIDCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();

        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        InventoryScreenId screenID = TargetedInventoryResolver.withTargetedScreen(player, TargetedScreenContext::screenId);

        if (screenID == null) {
            commandContext.getSource().sendSuccess(() -> ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.screenid.fail"), false);
            return 0;
        }

        MutableComponent feedbackText = ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.screenid.success", screenID.toString());

        Component copyableText = feedbackText
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.CopyToClipboard(screenID.toString()))
                        .withHoverEvent(new HoverEvent.ShowText(ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.screenid.copy.hover")))
                );

        commandContext.getSource().sendSuccess(() -> copyableText, false);
        return 1;
    }
}
