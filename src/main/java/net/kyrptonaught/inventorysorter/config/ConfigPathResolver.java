package net.kyrptonaught.inventorysorter.config;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class ConfigPathResolver {
    public static Path getConfigPath(String filePath) {
        return FabricLoader.getInstance().getConfigDir().resolve(filePath);
    }
}
