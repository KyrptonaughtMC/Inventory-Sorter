package net.kyrptonaught.inventorysorter.client.config;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class Config {
    public static NewConfigOptions load() {
        try {
            Path oldConfigPath = ConfigPathResolver.getConfigPath(OldConfigOptions.CONFIG_FILE);
            if (oldConfigPath.toFile().exists()) {
                LOGGER.info("Found old config file, converting to new format...");
                OldConfigOptions oldConfig = OldConfigOptions.load();
                NewConfigOptions newConfig = NewConfigOptions.convertOldToNew(oldConfig);
                newConfig.save();
                Files.walk(oldConfigPath.getParent())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                LOGGER.info("Old config file converted successfully.");
                return newConfig;
            } else {
                return NewConfigOptions.load();
            }
        } catch (IOException | SyntaxError e) {
            return new NewConfigOptions(); // Return default config in case of error
        }
    }
}
