package net.kyrptonaught.inventorysorter;

import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.InventorySorterPlatform;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlayerDataPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

class ServerPlayerJoinSyncTest {
    @Test
    void joiningPlayerAlwaysReceivesPresenceAndPreviousVersionThenStoresCurrentVersion() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingPlayerDataPlatform playerData = new RecordingPlayerDataPlatform();
        playerData.lastSeenVersion = new LastSeenVersionPacket("26.1.1", "zh_cn");
        ServerPlayerJoinSync joinSync = newJoinSync(networking, playerData, false, new NewConfigOptions(), "26.1.2");

        joinSync.syncJoiningPlayer(null, null, "EN_US");

        Assertions.assertEquals(List.of(
                new ServerPresencePacket(),
                new LastSeenVersionPacket("26.1.1", "zh_cn")
        ), networking.playerboundPayloads);
        Assertions.assertEquals(new LastSeenVersionPacket("26.1.2", "en_us"), playerData.lastSeenVersion);
    }

    @Test
    void dedicatedServerSendsStoredClientSettingsWhenClientHasNotSyncedBefore() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingPlayerDataPlatform playerData = new RecordingPlayerDataPlatform();
        playerData.clientSync = new ClientSync(false);
        playerData.playerSortPrevention = new PlayerSortPrevention(Set.of("minecraft:anvil"));
        playerData.sortSettings = new SortSettings(false, true, false, SortType.MOD);
        NewConfigOptions config = new NewConfigOptions();
        config.hideButtonsForScreens.add("minecraft:chest");
        ServerPlayerJoinSync joinSync = newJoinSync(networking, playerData, true, config, "26.1.2");

        joinSync.syncJoiningPlayer(null, null, "en_us");

        Assertions.assertEquals(List.of(
                new ServerPresencePacket(),
                LastSeenVersionPacket.DEFAULT,
                new PlayerSortPrevention(Set.of("minecraft:anvil")),
                new SortSettings(false, true, false, SortType.MOD),
                new HideButton(Set.of("minecraft:chest"))
        ), networking.playerboundPayloads);
    }

    @Test
    void dedicatedServerDoesNotResendStoredClientSettingsWhenClientHasSyncedBefore() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingPlayerDataPlatform playerData = new RecordingPlayerDataPlatform();
        playerData.clientSync = new ClientSync(true);
        playerData.playerSortPrevention = new PlayerSortPrevention(Set.of("minecraft:anvil"));
        playerData.sortSettings = new SortSettings(false, true, false, SortType.MOD);
        NewConfigOptions config = new NewConfigOptions();
        config.hideButtonsForScreens.add("minecraft:chest");
        ServerPlayerJoinSync joinSync = newJoinSync(networking, playerData, true, config, "26.1.2");

        joinSync.syncJoiningPlayer(null, null, "en_us");

        Assertions.assertEquals(List.of(
                new ServerPresencePacket(),
                LastSeenVersionPacket.DEFAULT,
                new HideButton(Set.of("minecraft:chest"))
        ), networking.playerboundPayloads);
    }

    @Test
    void integratedServerDoesNotSendDedicatedServerConfigState() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingPlayerDataPlatform playerData = new RecordingPlayerDataPlatform();
        playerData.clientSync = new ClientSync(false);
        playerData.playerSortPrevention = new PlayerSortPrevention(Set.of("minecraft:anvil"));
        playerData.sortSettings = new SortSettings(false, true, false, SortType.MOD);
        NewConfigOptions config = new NewConfigOptions();
        config.hideButtonsForScreens.add("minecraft:chest");
        ServerPlayerJoinSync joinSync = newJoinSync(networking, playerData, false, config, "26.1.2");

        joinSync.syncJoiningPlayer(null, null, "en_us");

        Assertions.assertEquals(List.of(
                new ServerPresencePacket(),
                LastSeenVersionPacket.DEFAULT
        ), networking.playerboundPayloads);
    }

    private ServerPlayerJoinSync newJoinSync(
            RecordingNetworkingPlatform networking,
            RecordingPlayerDataPlatform playerData,
            boolean dedicatedServer,
            NewConfigOptions config,
            String version
    ) {
        return new ServerPlayerJoinSync(
                networking,
                playerData,
                new RecordingInventorySorterPlatform(dedicatedServer),
                () -> config,
                version
        );
    }

    private static class RecordingNetworkingPlatform implements NetworkingPlatform {
        private final List<CustomPacketPayload> playerboundPayloads = new ArrayList<>();

        @Override
        public void registerPayloads() {
            throw new UnsupportedOperationException("Not needed for server join sync tests");
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
            throw new UnsupportedOperationException("Not needed for server join sync tests");
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            throw new UnsupportedOperationException("Not needed for server join sync tests");
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            playerboundPayloads.add(payload);
        }
    }

    private static class RecordingPlayerDataPlatform implements PlayerDataPlatform {
        private SortSettings sortSettings = SortSettings.DEFAULT;
        private PlayerSortPrevention playerSortPrevention = PlayerSortPrevention.DEFAULT;
        private ClientSync clientSync = ClientSync.DEFAULT;
        private LastSeenVersionPacket lastSeenVersion = LastSeenVersionPacket.DEFAULT;

        @Override
        public SortSettings getSortSettings(ServerPlayer player) {
            return sortSettings;
        }

        @Override
        public void setSortSettings(ServerPlayer player, SortSettings settings) {
            this.sortSettings = settings;
        }

        @Override
        public PlayerSortPrevention getPlayerSortPrevention(ServerPlayer player) {
            return playerSortPrevention;
        }

        @Override
        public void setPlayerSortPrevention(ServerPlayer player, PlayerSortPrevention value) {
            this.playerSortPrevention = value;
        }

        @Override
        public ClientSync getClientSync(ServerPlayer player) {
            return clientSync;
        }

        @Override
        public void setClientSync(ServerPlayer player, ClientSync value) {
            this.clientSync = value;
        }

        @Override
        public LastSeenVersionPacket getLastSeenVersion(ServerPlayer player) {
            return lastSeenVersion;
        }

        @Override
        public void setLastSeenVersion(ServerPlayer player, LastSeenVersionPacket value) {
            this.lastSeenVersion = value;
        }
    }

    private record RecordingInventorySorterPlatform(boolean dedicatedServer) implements InventorySorterPlatform {
        @Override
        public Path getConfigDir() {
            throw new UnsupportedOperationException("Not needed for server join sync tests");
        }

        @Override
        public boolean isDedicatedServer(MinecraftServer server) {
            return dedicatedServer;
        }
    }
}
