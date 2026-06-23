package net.kyrptonaught.inventorysorter.platform;

//? if fabric {
import net.kyrptonaught.inventorysorter.platform.fabric.FabricCommandPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricInventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricNetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.fabric.FabricPlayerDataPlatform;
//?}
//? if neoforge {
/*import net.kyrptonaught.inventorysorter.platform.neoforge.NeoForgeCommandPlatform;
import net.kyrptonaught.inventorysorter.platform.neoforge.NeoForgeInventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.neoforge.NeoForgeNetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.neoforge.NeoForgePlayerDataPlatform;
*///?}

public final class PlatformServices {
    //? if fabric {
    public static final InventorySorterPlatform PLATFORM = new FabricInventorySorterPlatform();
    public static final CommandPlatform COMMANDS = new FabricCommandPlatform();
    public static final NetworkingPlatform NETWORK = new FabricNetworkingPlatform();
    public static final PlayerDataPlatform PLAYER_DATA = new FabricPlayerDataPlatform();
    //? }

    //? if neoforge {
    /*public static final InventorySorterPlatform PLATFORM = new NeoForgeInventorySorterPlatform();
    public static final CommandPlatform COMMANDS = new NeoForgeCommandPlatform();
    public static final NetworkingPlatform NETWORK = new NeoForgeNetworkingPlatform();
    public static final PlayerDataPlatform PLAYER_DATA = new NeoForgePlayerDataPlatform();
    *///?}

    private PlatformServices() {
    }
}
