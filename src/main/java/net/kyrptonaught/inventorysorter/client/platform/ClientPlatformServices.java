package net.kyrptonaught.inventorysorter.client.platform;

//? if fabric {
import net.kyrptonaught.inventorysorter.client.platform.fabric.FabricClientKeyMappings;
//?}
//? if neoforge {
/*import net.kyrptonaught.inventorysorter.client.platform.neoforge.NeoForgeClientKeyMappings;
*///?}

public final class ClientPlatformServices {
    //? if fabric {
    public static final ClientKeyMappings KEY_MAPPINGS = new FabricClientKeyMappings();
    //?}
    //? if neoforge {
    /*public static final NeoForgeClientKeyMappings KEY_MAPPINGS = new NeoForgeClientKeyMappings();
    *///?}

    private ClientPlatformServices() {
    }
}
