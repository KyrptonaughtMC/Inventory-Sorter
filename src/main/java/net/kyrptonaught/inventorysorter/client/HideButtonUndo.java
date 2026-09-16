package net.kyrptonaught.inventorysorter.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HideButtonUndo {
    private static final String UNDO_COMMAND = InventorySorterMod.MOD_ID + ":undo_hide_button";

    public static <S> void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal(UNDO_COMMAND)
                .then(RequiredArgumentBuilder.<S, Identifier>argument("screenId", IdentifierArgument.id())
                        .executes(context -> undo(context.getArgument("screenId", Identifier.class).toString()))));
    }

    public static Component hideMessage(String screenId) {
        return Component.empty()
                .append(Component.literal("[Inventory Sorter] ").withColor(0xC9A55C))
                .append(Component.translatable("inventorysorter.sortButton.chat.hidden",
                        Component.literal(screenId).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" "))
                .append(Component.translatable("inventorysorter.sortButton.chat.undo")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA).withUnderlined(true)
                                .withClickEvent(new ClickEvent.RunCommand("/" + UNDO_COMMAND + " " + screenId))));
    }

    private static int undo(String screenId) {
        NewConfigOptions current = InventorySorterMod.getConfig();
        if (current.hideButtonsForScreens.contains(screenId)) {
            current.enableButtonForScreen(screenId);
            current.save();
            InventorySorterMod.compatibility.reload();
            ClientConfigSync.syncConfigToServer();
            Minecraft client = Minecraft.getInstance();
            //? >= 26.2
            client.gui.chatListener().handleSystemMessage(restoredMessage(screenId), false);
            //? < 26.2
            //client.getChatListener().handleSystemMessage(restoredMessage(screenId), false);
            return 1;
        }
        return 0;
    }

    static Component restoredMessage(String screenId) {
        return Component.empty()
                .append(Component.literal("[Inventory Sorter] ").withColor(0xC9A55C))
                .append(Component.translatable("inventorysorter.sortButton.chat.restored",
                        Component.literal(screenId).withStyle(ChatFormatting.GRAY)).withColor(0x9CCB8E))
                .append(Component.literal(" "))
                .append(Component.translatable("inventorysorter.sortButton.chat.reopen").withStyle(ChatFormatting.GRAY));
    }
}
