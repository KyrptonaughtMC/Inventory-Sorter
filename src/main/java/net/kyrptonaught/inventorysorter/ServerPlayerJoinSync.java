package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.platform.PlayerDataPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.VERSION;

public class ServerPlayerJoinSync {
    private final NetworkingPlatform networking;
    private final PlayerDataPlatform playerData;
    private final InventorySorterPlatform platform;
    private final Supplier<NewConfigOptions> config;
    private final String version;

    public ServerPlayerJoinSync() {
        this(
                PlatformServices.NETWORK,
                PlatformServices.PLAYER_DATA,
                PlatformServices.PLATFORM,
                InventorySorterMod::getConfig,
                VERSION
        );
    }

    ServerPlayerJoinSync(
            NetworkingPlatform networking,
            PlayerDataPlatform playerData,
            InventorySorterPlatform platform,
            Supplier<NewConfigOptions> config,
            String version
    ) {
        this.networking = networking;
        this.playerData = playerData;
        this.platform = platform;
        this.config = config;
        this.version = version;
    }

    /**
     * Synchronizes server-owned state to a player when the loader reports that they joined.
     *
     * Loader initializers should adapt their join event into this method. The method sends the
     * server presence/version handshake for every server, then sends server-only config state when
     * running on a dedicated server.
     */
    public void syncJoiningPlayer(ServerPlayer player, MinecraftServer server) {
        syncJoiningPlayer(player, server, player.clientInformation().language());
    }

    void syncJoiningPlayer(ServerPlayer player, MinecraftServer server, String language) {
        networking.sendToPlayer(player, new ServerPresencePacket());
        networking.sendToPlayer(player, playerData.getLastSeenVersion(player));
        playerData.setLastSeenVersion(player, new LastSeenVersionPacket(version, language.toLowerCase(Locale.ROOT)));

        if (platform.isDedicatedServer(server)) {
            if (!playerData.getClientSync(player).seenClient()) {
                syncStoredClientSettings(player);
            }

            networking.sendToPlayer(player, HideButton.fromConfig(config.get()));
        }
    }

    private void syncStoredClientSettings(ServerPlayer player) {
        PlayerSortPrevention sortPrevention = playerData.getPlayerSortPrevention(player);
        if (sortPrevention != PlayerSortPrevention.DEFAULT) {
            networking.sendToPlayer(player, sortPrevention);
        }

        SortSettings sortSettings = playerData.getSortSettings(player);
        if (sortSettings != SortSettings.DEFAULT) {
            networking.sendToPlayer(player, sortSettings);
        }
    }
}
