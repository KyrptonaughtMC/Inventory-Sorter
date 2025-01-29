package net.kyrptonaught.inventorysorter.compat.sources;

import net.kyrptonaught.inventorysorter.client.config.NewConfigOptions;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.compat.sources.OnlineData.RemoteConfigData;

public class ConfigLoader implements CompatibilityLoader {
    private final NewConfigOptions config;
    private RemoteConfigData remoteData = new RemoteConfigData();

    public ConfigLoader(NewConfigOptions config) {
        this.config = config;
        if (this.config.customCompatibilityListDownloadUrl != null && !this.config.customCompatibilityListDownloadUrl.isEmpty()) {
            try {
                URL url = URI.create(this.config.customCompatibilityListDownloadUrl).toURL();
                this.remoteData = RemoteConfigData.downloadFrom(url);
            } catch (Exception e) {
                LOGGER.error("Not a valid URL in the config file: {}", this.config.customCompatibilityListDownloadUrl);
            }
        }
    }

    @Override
    public Set<Identifier> getPreventSort() {
        Set<Identifier> preventSort = new HashSet<>();

        if (config.preventSortForScreens != null) {
            preventSort.addAll(config.preventSortForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));
        }

        preventSort.addAll(remoteData.preventSortForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));

        return preventSort;
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        Set<Identifier> shouldHideSortButtons = new HashSet<>();

        if (config.hideButtonsForScreens != null) {
            shouldHideSortButtons.addAll(config.hideButtonsForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));
        }

        shouldHideSortButtons.addAll(remoteData.hideButtonsForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));

        return shouldHideSortButtons;
    }
}
