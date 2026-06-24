/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.client.platform.neoforge;

import net.kyrptonaught.inventorysorter.client.ConfigScreen;
import net.kyrptonaught.inventorysorter.client.InventorySorterClientRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private static InventorySorterClientRuntime runtime;

    private NeoForgeClientEvents() {
    }

    static void initialize(InventorySorterClientRuntime clientRuntime) {
        runtime = clientRuntime;
    }

    @SubscribeEvent
    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (runtime != null) {
            runtime.clientServerSession().join(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        if (runtime != null) {
            runtime.clientServerSession().disconnect();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (runtime == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ConfigScreen.openIfConfigKeyPressed(client);
        runtime.clientServerSession().tick(client);
        runtime.clientSortRuntime().tickClickExecutor(client);
    }
}
*//*?}*/
