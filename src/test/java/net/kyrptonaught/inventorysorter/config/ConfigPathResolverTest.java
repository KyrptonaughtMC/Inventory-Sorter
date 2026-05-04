package net.kyrptonaught.inventorysorter.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;

public class ConfigPathResolverTest {
    @Test
    void resolvesFilePathFromPlatformConfigDir() {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path configDir = fileSystem.getPath("/minecraft/config");

            Path configPath = ConfigPathResolver.getConfigPath("inventorysorter.json5", new TestPlatform(configDir));

            Assertions.assertEquals(configDir.resolve("inventorysorter.json5"), configPath);
        } catch (Exception e) {
            throw new AssertionError("Failed to close in-memory file system", e);
        }
    }

    private record TestPlatform(Path configDir) implements InventorySorterPlatform {
        @Override
        public Path getConfigDir() {
            return configDir;
        }

        @Override
        public boolean isDedicatedServer(MinecraftServer server) {
            throw new UnsupportedOperationException("Not needed for config path tests");
        }
    }
}
