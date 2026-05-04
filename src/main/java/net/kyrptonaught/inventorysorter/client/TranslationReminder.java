package net.kyrptonaught.inventorysorter.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;

import java.net.URI;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class TranslationReminder {
    // replaced at build time by scripts/patch-completed-langs.sh
    private static final List<String> completedLanguages = List.of("KNOWN_LANGUAGES_REPL");

    public static void notifyIfOutdated(Minecraft client, LastSeenVersionPacket lastSeenVersion, String currentVersion) {
        String selectedLanguage = client.getLanguageManager().getSelected().toLowerCase();
        if (hasSeenCurrentVersion(lastSeenVersion, currentVersion, selectedLanguage)) {
            return;
        }

        notify(client);
    }

    static boolean hasSeenCurrentVersion(LastSeenVersionPacket lastSeenVersion, String currentVersion, String selectedLanguage) {
        return lastSeenVersion.lastSeenVersion().equals(currentVersion)
                && lastSeenVersion.lastSeenLanguage().equals(selectedLanguage.toLowerCase());
    }

    public static void notify(Minecraft client) {
        String languageCode = client.getLanguageManager().getSelected().toLowerCase();

        if (completedLanguages.contains(languageCode)) {
            return;
        }

        if (languageCode.startsWith("en_")) {
            return;
        }

        LanguageInfo language = client.getLanguageManager().getLanguage(languageCode);
        if (language == null) {
            return;
        }

        if (client.player != null) {
            URI crowdinUri = URI.create("https://crowdin.com/project/inventory-sorter");
            MutableComponent crowdinTooltip = Component.translatable(MOD_ID + ".cmd.crowdin.tooltip");

            ClickEvent.OpenUrl clickEvent = new ClickEvent.OpenUrl(crowdinUri);
            HoverEvent.ShowText showText = new HoverEvent.ShowText(crowdinTooltip);

            client.player.sendSystemMessage(
                    Component.translatable(MOD_ID + ".cmd.translate", Component.literal("Inventory Sorter").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GOLD))).withStyle(style -> style.withColor(ChatFormatting.AQUA))
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable(MOD_ID + ".cmd.crowdin").withStyle(style -> style.withBold(true)
                                    .withColor(ChatFormatting.BLUE)
                                    .withUnderlined(true)
                                    .withHoverEvent(showText)
                                    .withClickEvent(clickEvent)))
            );
        }
    }
}
