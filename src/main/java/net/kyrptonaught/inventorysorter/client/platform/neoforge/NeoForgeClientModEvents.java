/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.client.platform.neoforge;

import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.client.platform.ClientPlatformServices;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientModEvents {
    private NeoForgeClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlatformServices.COMMANDS.registerCommands();
        PlatformServices.NETWORK.registerPayloads();
        NeoForgeClientEvents.initialize(InventorySorterModClient.initializeClient());
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        ClientPlatformServices.KEY_MAPPINGS.register(event);
    }
}
*//*?}*/
