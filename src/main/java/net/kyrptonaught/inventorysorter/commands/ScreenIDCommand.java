package net.kyrptonaught.inventorysorter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.permissions.CommandPermission;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ScreenIDCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, LiteralArgumentBuilder<ServerCommandSource> rootCommand) {
        dispatcher.register(rootCommand.then(CommandManager.literal("screenID")
                .requires(CommandPermission.require("screenid", 0))
                .executes(ScreenIDCommand::run)));
    }

    public static int run(CommandContext<ServerCommandSource> commandContext) {
        ServerPlayerEntity player = commandContext.getSource().getPlayer();

        if (player == null) {
            commandContext.getSource().sendFeedback(CommandTranslations::playerRequired, false);
            return 0;
        }

        Identifier screenID = InventoryHelper.withTargetedScreenHandler(player, InventoryHelper.ScreenContext::screenId);

        if (screenID == null) {
            commandContext.getSource().sendFeedback(() -> Text.translatable("inventorysorter.cmd.screenid.fail"), false);
            return 0;
        }

        MutableText feedbackText = Text.translatable("inventorysorter.cmd.screenid.success", screenID.toString());

        /*? if >=1.21.5 {*/
        Text copyableText = feedbackText
                .styled(style -> style
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent.CopyToClipboard(screenID.toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("inventorysorter.cmd.screenid.copy.hover")))
                );
        /*?} else {*/

        /*Text copyableText = feedbackText
                .styled(style -> style
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, screenID.toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("inventorysorter.cmd.screenid.copy.hover")))
                );
        *//*?}*/


        commandContext.getSource().sendFeedback(() -> copyableText, false);
        return 1;
    }
}
