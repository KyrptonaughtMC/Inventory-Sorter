package net.kyrptonaught.inventorysorter.compat.sources;

import com.google.gson.Gson;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.config.SchemaValidator;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class RemoteConfigLoader implements CompatibilityLoader {
    private final Supplier<String> customCompatibilityListDownloadUrl;
    private CompatConfig config = new CompatConfig();
    private long lastRemoteConfigFetch = 0;
    private static final long CACHE_TTL_MILLIS = 2 * 1000; // 2 seconds small cache TTL

    public RemoteConfigLoader(Supplier<String> customCompatibilityListDownloadUrl) {
        this.customCompatibilityListDownloadUrl = customCompatibilityListDownloadUrl;
    }

    private void loadRemoteConfig() {
        long now = System.currentTimeMillis();
        boolean shouldRefresh = (now - lastRemoteConfigFetch > CACHE_TTL_MILLIS);

        if (shouldRefresh) {
            config.hideButtonsForScreens.clear();
            config.preventSortForScreens.clear();
            if (this.customCompatibilityListDownloadUrl.get() != null && !this.customCompatibilityListDownloadUrl.get().isEmpty()) {
                try {
                    URL url = URI.create(this.customCompatibilityListDownloadUrl.get()).toURL();
                    config = downloadFrom(url);
                    lastRemoteConfigFetch = now;
                } catch (Exception e) {
                    LOGGER.error("Not a valid URL in the config file: {}", this.customCompatibilityListDownloadUrl);
                }
            }
        }
    }

    public CompatConfig downloadFrom(URL url) {
        LOGGER.info("Downloading remote compatibility config from {}", url);
        try {
            Reader reader = new InputStreamReader(url.openStream());
            boolean success = SchemaValidator.isValidJsonObject(reader, SchemaValidator.REMOTE_CONFIG_SCHEMA, url.toString());

            if (success) {
                // re-read since JSONTokener consumes the stream
                reader = new InputStreamReader(url.openStream());
                return new Gson().fromJson(reader, CompatConfig.class);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load remote config from: {}", url, e);
        }
        return new CompatConfig();
    }

    @Override
    public Set<Identifier> getPreventSort() {
        loadRemoteConfig();
        return config.preventSortForScreens.stream()
                .map(Identifier::of)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        loadRemoteConfig();
        return config.hideButtonsForScreens.stream()
                .map(Identifier::of)
                .collect(Collectors.toSet());
    }
}


