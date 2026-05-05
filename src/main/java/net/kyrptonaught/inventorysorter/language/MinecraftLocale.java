package net.kyrptonaught.inventorysorter.language;

import java.util.Locale;

public final class MinecraftLocale {
    private MinecraftLocale() {
    }

    public static Locale fromLanguageCode(String languageCode) {
        String[] parts = languageCode.toLowerCase().split("_");
        if (parts.length == 2) {
            return Locale.of(parts[0], parts[1].toUpperCase());
        }
        return Locale.getDefault();
    }
}
