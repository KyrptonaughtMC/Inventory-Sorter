package net.kyrptonaught.inventorysorter.platform;

import net.kyrptonaught.inventorysorter.platform.fabric.FabricInventorySorterPlatform;

public final class PlatformServices {
    public static final InventorySorterPlatform PLATFORM = new FabricInventorySorterPlatform();

    private PlatformServices() {
    }
}
