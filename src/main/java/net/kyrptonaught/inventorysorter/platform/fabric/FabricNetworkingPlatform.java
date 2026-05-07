package net.kyrptonaught.inventorysorter.platform.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class FabricNetworkingPlatform implements NetworkingPlatform {
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(ClientSync.ID, ClientSync.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(LastSeenVersionPacket.ID, LastSeenVersionPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerPresencePacket.ID, ServerPresencePacket.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(HideButton.ID, HideButton.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReloadConfigPacket.ID, ReloadConfigPacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(SortSettings.ID, SortSettings.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SortSettings.ID, SortSettings.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(InventorySortPacket.ID, InventorySortPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(InventorySortPacket.ID, ((payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = player.level().getServer();
            server.execute(() -> ServerInventorySorter.sort(
                    player,
                    payload.target(),
                    PlatformServices.PLAYER_DATA.getSortSettings(player).withSortType(payload.sortType())
            ));
        }));

        ServerPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> {
            PlatformServices.PLAYER_DATA.setSortSettings(context.player(), payload);
        });

        ServerPlayNetworking.registerGlobalReceiver(PlayerSortPrevention.ID, (payload, context) -> {
            PlatformServices.PLAYER_DATA.setPlayerSortPrevention(context.player(), payload);
        });

        ServerPlayNetworking.registerGlobalReceiver(ClientSync.ID, (payload, context) -> {
            PlatformServices.PLAYER_DATA.setClientSync(context.player(), new ClientSync(true));
        });
    }

    @Override
    public void registerClientReceivers(
            Consumer<SortSettings> sortSettingsHandler,
            Consumer<PlayerSortPrevention> playerSortPreventionHandler,
            Consumer<HideButton> hideButtonHandler,
            Runnable reloadConfigHandler,
            Consumer<LastSeenVersionPacket> lastSeenVersionHandler,
            Runnable serverPresenceHandler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> sortSettingsHandler.accept(payload));
        ClientPlayNetworking.registerGlobalReceiver(PlayerSortPrevention.ID, (payload, context) -> playerSortPreventionHandler.accept(payload));
        ClientPlayNetworking.registerGlobalReceiver(HideButton.ID, (payload, context) -> hideButtonHandler.accept(payload));
        ClientPlayNetworking.registerGlobalReceiver(ReloadConfigPacket.ID, (payload, context) -> reloadConfigHandler.run());
        ClientPlayNetworking.registerGlobalReceiver(LastSeenVersionPacket.ID, (payload, context) -> lastSeenVersionHandler.accept(payload));
        ClientPlayNetworking.registerGlobalReceiver(ServerPresencePacket.ID, (payload, context) -> serverPresenceHandler.run());
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
