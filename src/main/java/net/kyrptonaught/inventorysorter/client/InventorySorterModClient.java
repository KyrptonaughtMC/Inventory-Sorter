package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.client.platform.ClientPlatformServices;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        InventorySorterClientRuntime runtime = InventorySorterClientRuntime.create();

        ClientPlatformServices.KEY_MAPPINGS.register();

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(runtime.clientPacketReceivers()::serverConfig));


        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            runtime.clientServerSession().join(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            runtime.clientServerSession().disconnect();
        });

        ClientTickEvents.END_CLIENT_TICK.register(ConfigScreen::openIfConfigKeyPressed);
        ClientTickEvents.END_CLIENT_TICK.register(runtime.clientServerSession()::tick);
        ClientTickEvents.END_CLIENT_TICK.register(runtime.clientSortRuntime()::tickClickExecutor);

        runtime.clientPacketReceivers().register(PlatformServices.NETWORK);
    }
}
