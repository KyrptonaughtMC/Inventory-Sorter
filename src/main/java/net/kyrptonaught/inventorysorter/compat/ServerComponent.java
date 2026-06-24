package net.kyrptonaught.inventorysorter.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerComponent {
    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final Map<String, Optional<Map<String, String>>> TRANSLATIONS = new ConcurrentHashMap<>();

    public static MutableComponent translate(String key, Object... args) {
        return lang(DEFAULT_LANGUAGE).translate(key, args);
    }

    public static Translator lang(String languageCode) {
        Objects.requireNonNull(languageCode, "languageCode");

        if (languageCode.isBlank()) {
            throw new IllegalArgumentException("languageCode must not be blank");
        }

        return new Translator(languageCode.toLowerCase(Locale.ROOT));
    }

    public record Translator(String languageCode) {
        public MutableComponent translate(String key, Object... args) {
            Optional<String> fallback = fallbackFor(languageCode, key);

            if (fallback.isEmpty() && !DEFAULT_LANGUAGE.equals(languageCode)) {
                fallback = fallbackFor(DEFAULT_LANGUAGE, key);
            }

            return Component.translatableWithFallback(key, fallback.orElse(null), args);
        }
    }

    private static Optional<String> fallbackFor(String languageCode, String key) {
        return translationsFor(languageCode)
                .flatMap(translations -> translations.containsKey(key)
                        ? Optional.of(translations.get(key))
                        : Optional.empty());
    }

    private static Optional<Map<String, String>> translationsFor(String languageCode) {
        return TRANSLATIONS.computeIfAbsent(languageCode, ServerComponent::loadTranslations);
    }

    private static Optional<Map<String, String>> loadTranslations(String languageCode) {
        String path = "data/inventorysorter/lang/" + languageCode + ".json";
        try (InputStream stream = ServerComponent.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return Optional.empty();
            }

            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                Map<String, String> translations = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    translations.put(entry.getKey(), entry.getValue().getAsString());
                }

                return Optional.of(Map.copyOf(translations));
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
