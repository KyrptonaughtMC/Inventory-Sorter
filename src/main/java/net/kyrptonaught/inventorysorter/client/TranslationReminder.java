package net.kyrptonaught.inventorysorter.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class TranslationReminder {
    public static void notify(MinecraftClient client) {
        String languageCode = client.getLanguageManager().getLanguage().toLowerCase();
        if (languageCode.startsWith("en_")) {
            return;
        }

        LanguageDefinition language = client.getLanguageManager().getLanguage(languageCode);
        if (language == null) {
            return;
        }

        if (client.player != null) {
            URI crowdinUri = URI.create("https://crowdin.com/project/inventory-sorter");
            MutableText crowdinTooltip = Text.translatable(MOD_ID + ".cmd.crowdin.tooltip");

            /*? if > 1.21.4 {*/
            ClickEvent.OpenUrl clickEvent = new ClickEvent.OpenUrl(crowdinUri);
            HoverEvent.ShowText showText = new HoverEvent.ShowText(crowdinTooltip);
            /*?} else {*/
            /*ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, crowdinUri.toString());
            HoverEvent showText = new HoverEvent(HoverEvent.Action.SHOW_TEXT, crowdinTooltip);
            *//*?}*/

            client.player.sendMessage(
                    Text.translatable(MOD_ID + ".cmd.translate", Text.literal("Inventory Sorter").styled(style -> style.withBold(true).withColor(Formatting.GOLD))).styled(style -> style.withColor(Formatting.AQUA))
                            .append(Text.literal("\n\n"))
                            .append(Text.translatable(MOD_ID + ".cmd.crowdin").styled(style -> style.withBold(true)
                                    .withColor(Formatting.BLUE)
                                    .withUnderline(true)
                                    .withHoverEvent(showText)
                                    .withClickEvent(clickEvent))),
                    false
            );
        }
    }
}
