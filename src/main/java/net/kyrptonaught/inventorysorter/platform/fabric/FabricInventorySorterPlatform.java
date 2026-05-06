package net.kyrptonaught.inventorysorter.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class FabricInventorySorterPlatform implements InventorySorterPlatform {
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isDedicatedServer(MinecraftServer server) {
        return server.isDedicatedServer();
    }
}
