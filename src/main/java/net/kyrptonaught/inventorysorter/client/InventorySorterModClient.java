package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.client.platform.ClientPlatformServices;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.resources.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    public static Identifier PLAYER_INVENTORY = Identifier.parse("player_inventory");
    private final ClientPacketReceivers clientPacketReceivers = new ClientPacketReceivers();
    private final ClientServerSession clientServerSession = new ClientServerSession(clientPacketReceivers);

    public static void syncConfig() {
        ClientConfigSync.syncConfigToServer(getConfig(), PlatformServices.NETWORK);
    }

    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(clientServerSession::shutdown));

        ClientPlatformServices.KEY_MAPPINGS.register();

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(clientPacketReceivers::serverConfig));


        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            clientServerSession.join(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clientServerSession.disconnect();
        });

        ClientTickEvents.END_CLIENT_TICK.register(ConfigScreen::openIfConfigKeyPressed);

        clientPacketReceivers.register(PlatformServices.NETWORK);
    }
}
