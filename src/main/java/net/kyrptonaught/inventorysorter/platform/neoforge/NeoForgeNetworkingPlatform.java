/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.platform.neoforge;

import net.kyrptonaught.inventorysorter.inventory.ServerInventorySorter;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class NeoForgeNetworkingPlatform implements NetworkingPlatform {
    private static Consumer<SortSettings> sortSettingsHandler = payload -> {
    };
    private static Consumer<PlayerSortPrevention> playerSortPreventionHandler = payload -> {
    };
    private static Consumer<HideButton> hideButtonHandler = payload -> {
    };
    private static Runnable reloadConfigHandler = () -> {
    };
    private static Consumer<LastSeenVersionPacket> lastSeenVersionHandler = payload -> {
    };
    private static Runnable serverPresenceHandler = () -> {
    };

    @Override
    public void registerPayloads() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToServer(InventorySortPacket.ID, InventorySortPacket.CODEC, (payload, context) -> {
            Player contextPlayer = context.player();
            if (contextPlayer instanceof ServerPlayer player) {
                MinecraftServer server = player.level().getServer();
                if (server != null) {
                    server.execute(() -> ServerInventorySorter.sort(
                            player,
                            payload.target(),
                            PlatformServices.PLAYER_DATA.getSortSettings(player).withSortType(payload.sortType())
                    ));
                }
            }
        });

        registrar.playBidirectional(SortSettings.ID, SortSettings.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                PlatformServices.PLAYER_DATA.setSortSettings(player, payload);
            }
        });

        registrar.playBidirectional(PlayerSortPrevention.ID, PlayerSortPrevention.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                PlatformServices.PLAYER_DATA.setPlayerSortPrevention(player, payload);
            }
        });

        registrar.playToServer(ClientSync.ID, ClientSync.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                PlatformServices.PLAYER_DATA.setClientSync(player, new ClientSync(true));
            }
        });

        registrar.playToClient(LastSeenVersionPacket.ID, LastSeenVersionPacket.CODEC);
        registrar.playToClient(ServerPresencePacket.ID, ServerPresencePacket.CODEC);
        registrar.playToClient(HideButton.ID, HideButton.CODEC);
        registrar.playToClient(ReloadConfigPacket.ID, ReloadConfigPacket.CODEC);
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
        NeoForgeNetworkingPlatform.sortSettingsHandler = sortSettingsHandler;
        NeoForgeNetworkingPlatform.playerSortPreventionHandler = playerSortPreventionHandler;
        NeoForgeNetworkingPlatform.hideButtonHandler = hideButtonHandler;
        NeoForgeNetworkingPlatform.reloadConfigHandler = reloadConfigHandler;
        NeoForgeNetworkingPlatform.lastSeenVersionHandler = lastSeenVersionHandler;
        NeoForgeNetworkingPlatform.serverPresenceHandler = serverPresenceHandler;
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientPayloadHandlers {
        @SubscribeEvent
        public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
            event.register(SortSettings.ID, (payload, context) -> sortSettingsHandler.accept(payload));
            event.register(PlayerSortPrevention.ID, (payload, context) -> playerSortPreventionHandler.accept(payload));
            event.register(HideButton.ID, (payload, context) -> hideButtonHandler.accept(payload));
            event.register(ReloadConfigPacket.ID, (payload, context) -> reloadConfigHandler.run());
            event.register(LastSeenVersionPacket.ID, (payload, context) -> lastSeenVersionHandler.accept(payload));
            event.register(ServerPresencePacket.ID, (payload, context) -> serverPresenceHandler.run());
        }
    }
}
*//*?}*/
