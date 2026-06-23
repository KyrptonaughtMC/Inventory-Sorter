/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.platform.neoforge;

import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgeInventorySorterPlatform implements InventorySorterPlatform {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDedicatedServer(MinecraftServer server) {
        return server.isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
*//*?}*/
