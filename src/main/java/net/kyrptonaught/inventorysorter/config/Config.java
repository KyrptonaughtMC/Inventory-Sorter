package net.kyrptonaught.inventorysorter.config;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class Config {
    private static final Path oldConfigPath = ConfigPathResolver.getConfigPath(OldConfigOptions.CONFIG_FILE);
    private static final Path oldCompatibilityListPath = ConfigPathResolver.getConfigPath(OldCompatibilityListOptions.CONFIG_FILE);

    public static NewConfigOptions load() {
        NewConfigOptions newConfig = convertedOptions();

        if (oldCompatibilityListPath.toFile().exists()) {
            LOGGER.info("Found old compatibility file, converting to new format...");
            OldCompatibilityListOptions denyListOptions = OldCompatibilityListOptions.load();
            newConfig.customCompatibilityListDownloadUrl = denyListOptions.blacklistDownloadURL;
            newConfig.hideButtonsForScreens = denyListOptions.hideSortBtnsList;
            newConfig.preventSortForScreens = denyListOptions.doNotSortList;
            LOGGER.info("Old compatibility file converted successfully.");
        }

        try {
            if (oldConfigPath.toFile().exists() || oldCompatibilityListPath.toFile().exists()) {
                Files.walk(oldConfigPath.getParent())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to delete old config files", e);
        }
        newConfig.save();
        return newConfig;
    }

    private static NewConfigOptions convertedOptions() {
        try {
            if (oldConfigPath.toFile().exists()) {
                LOGGER.info("Found old config file, converting to new format...");
                OldConfigOptions oldConfig = OldConfigOptions.load();
                NewConfigOptions newConfig = NewConfigOptions.convertOldToNew(oldConfig);
                LOGGER.info("Old config file converted successfully.");
                return newConfig;
            } else {
                return NewConfigOptions.load();
            }
        } catch (IOException | SyntaxError e) {
            return new NewConfigOptions();
        }
    }
}
