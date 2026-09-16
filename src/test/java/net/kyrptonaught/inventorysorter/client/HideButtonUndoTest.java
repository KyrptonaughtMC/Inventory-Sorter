package net.kyrptonaught.inventorysorter.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HideButtonUndoTest {
    @Test
    void hideMessageHasStyledInventoryIdAndOnlyUndoIsClickable() {
        Component message = HideButtonUndo.hideMessage("minecraft:chest");
        assertEquals(Style.EMPTY, message.getStyle());
        Component prefix = message.getSiblings().getFirst();
        assertEquals("[Inventory Sorter] ", prefix.getString());
        assertEquals(TextColor.fromRgb(0xC9A55C), prefix.getStyle().getColor());
        assertNull(prefix.getStyle().getClickEvent());
        Component sentence = message.getSiblings().get(1);
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.WHITE), sentence.getStyle().getColor());
        assertNull(sentence.getStyle().getClickEvent());
        TranslatableContents feedback = assertInstanceOf(TranslatableContents.class, sentence.getContents());
        assertEquals("inventorysorter.sortButton.chat.hidden", feedback.getKey());
        assertEquals(1, feedback.getArgs().length);
        Component screenId = assertInstanceOf(Component.class, feedback.getArgs()[0]);
        assertEquals("minecraft:chest", screenId.getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY), screenId.getStyle().getColor());
        assertNull(screenId.getStyle().getClickEvent());
        assertEquals(" ", message.getSiblings().get(2).getString());
        assertNull(message.getSiblings().get(2).getStyle().getClickEvent());
        Component link = message.getSiblings().getLast();
        assertEquals("inventorysorter.sortButton.chat.undo", assertInstanceOf(TranslatableContents.class, link.getContents()).getKey());
        assertTrue(link.getStyle().isUnderlined());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA), link.getStyle().getColor());
        ClickEvent.RunCommand click = assertInstanceOf(ClickEvent.RunCommand.class, link.getStyle().getClickEvent());
        assertEquals("/inventorysorter:undo_hide_button minecraft:chest", click.command());
    }

    @Test
    void restoredMessageHasSoftGreenConfirmationAndGreyReopenInstruction() {
        Component confirmation = HideButtonUndo.restoredMessage("minecraft:furnace");
        assertEquals(Style.EMPTY, confirmation.getStyle());
        assertEquals(TextColor.fromRgb(0xC9A55C), confirmation.getSiblings().getFirst().getStyle().getColor());
        Component sentence = confirmation.getSiblings().get(1);
        assertEquals(TextColor.fromRgb(0x9CCB8E), sentence.getStyle().getColor());
        TranslatableContents restored = assertInstanceOf(TranslatableContents.class, sentence.getContents());
        assertEquals("inventorysorter.sortButton.chat.restored", restored.getKey());
        Component screenId = assertInstanceOf(Component.class, restored.getArgs()[0]);
        assertEquals("minecraft:furnace", screenId.getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY), screenId.getStyle().getColor());
        assertNull(screenId.getStyle().getClickEvent());
        Component reopen = confirmation.getSiblings().getLast();
        assertEquals("inventorysorter.sortButton.chat.reopen", assertInstanceOf(TranslatableContents.class, reopen.getContents()).getKey());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY), reopen.getStyle().getColor());
        assertEquals(" ", confirmation.getSiblings().get(2).getString());
        confirmation.getSiblings().forEach(component -> assertNull(component.getStyle().getClickEvent()));
    }

    @Test
    void registeredCommandParsesTheInventoryInTheUndoLink() {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        HideButtonUndo.register(dispatcher);
        Component link = HideButtonUndo.hideMessage("example:container/chest").getSiblings().getLast();
        String command = assertInstanceOf(ClickEvent.RunCommand.class, link.getStyle().getClickEvent()).command().substring(1);
        var parsed = dispatcher.parse(command, new Object());
        assertFalse(parsed.getReader().canRead());
        assertTrue(parsed.getExceptions().isEmpty());
        var context = parsed.getContext().build(command);
        assertEquals(Identifier.parse("example:container/chest"), context.getArgument("screenId", Identifier.class));
        assertNotNull(context.getCommand());
    }

    @Test
    void registeredCommandRejectsMissingOrInvalidInventoryIds() {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        HideButtonUndo.register(dispatcher);
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("inventorysorter:undo_hide_button", new Object()));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("inventorysorter:undo_hide_button Invalid:screen", new Object()));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("inventorysorter:undo_hide_button minecraft:chest extra", new Object()));
    }
}
