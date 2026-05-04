package net.kyrptonaught.inventorysorter.platform;

import net.kyrptonaught.inventorysorter.platform.fabric.FabricInventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricNetworkingPlatform;

public final class PlatformServices {
    public static final InventorySorterPlatform PLATFORM = new FabricInventorySorterPlatform();
    public static final NetworkingPlatform NETWORK = new FabricNetworkingPlatform();

    private PlatformServices() {
    }
}
