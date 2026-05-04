package net.kyrptonaught.inventorysorter.client.platform;

import net.kyrptonaught.inventorysorter.client.platform.fabric.FabricClientKeyMappings;

public final class ClientPlatformServices {
    public static final ClientKeyMappings KEY_MAPPINGS = new FabricClientKeyMappings();

    private ClientPlatformServices() {
    }
}
