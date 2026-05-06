package net.kyrptonaught.inventorysorter.platform;

import net.kyrptonaught.inventorysorter.network.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public interface NetworkingPlatform {
    void registerPayloads();

    void registerClientReceivers(
            Consumer<SortSettings> sortSettingsHandler,
            Consumer<PlayerSortPrevention> playerSortPreventionHandler,
            Consumer<HideButton> hideButtonHandler,
            Runnable reloadConfigHandler,
            Consumer<LastSeenVersionPacket> lastSeenVersionHandler,
            Runnable serverPresenceHandler
    );

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
