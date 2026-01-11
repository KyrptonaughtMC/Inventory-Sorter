package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ScreenIDCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> rootCommand) {
        dispatcher.register(rootCommand.then(Commands.literal("screenID")
                .requires(CommandPermission.require("screenid", 0))
                .executes(ScreenIDCommand::run)));
    }

    public static int run(CommandContext<CommandSourceStack> commandContext) {
        ServerPlayer player = commandContext.getSource().getPlayer();

        if (player == null) {
            commandContext.getSource().sendSuccess(CommandTranslations::playerRequired, false);
            return 0;
        }

        Identifier screenID = InventoryHelper.withTargetedScreenHandler(player, InventoryHelper.ScreenContext::screenId);

        if (screenID == null) {
            commandContext.getSource().sendSuccess(() -> Component.translatable("inventorysorter.cmd.screenid.fail"), false);
            return 0;
        }

        MutableComponent feedbackText = Component.translatable("inventorysorter.cmd.screenid.success", screenID.toString());

        Component copyableText = feedbackText
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.CopyToClipboard(screenID.toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("inventorysorter.cmd.screenid.copy.hover")))
                );

        commandContext.getSource().sendSuccess(() -> copyableText, false);
        return 1;
    }
}
