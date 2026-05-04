package net.kyrptonaught.inventorysorter.platform;

import net.kyrptonaught.inventorysorter.platform.fabric.FabricCommandPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricInventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricNetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricPlayerDataPlatform;

public final class PlatformServices {
    public static final InventorySorterPlatform PLATFORM = new FabricInventorySorterPlatform();
    public static final CommandPlatform COMMANDS = new FabricCommandPlatform();
    public static final NetworkingPlatform NETWORK = new FabricNetworkingPlatform();
    public static final PlayerDataPlatform PLAYER_DATA = new FabricPlayerDataPlatform();

    private PlatformServices() {
    }
}
