package net.kyrptonaught.inventorysorter.platform;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public interface InventorySorterPlatform {
    Path getConfigDir();

    boolean isDedicatedServer(MinecraftServer server);
}
