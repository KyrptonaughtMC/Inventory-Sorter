package net.kyrptonaught.inventorysorter.config;

import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;

import java.nio.file.Path;

public class ConfigPathResolver {
    public static Path getConfigPath(String filePath) {
        return getConfigPath(filePath, PlatformServices.PLATFORM);
    }

    static Path getConfigPath(String filePath, InventorySorterPlatform platform) {
        return platform.getConfigDir().resolve(filePath);
    }
}
