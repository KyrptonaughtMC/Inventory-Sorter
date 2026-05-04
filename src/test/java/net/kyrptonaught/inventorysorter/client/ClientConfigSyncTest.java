package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ClientConfigSyncTest {
    @Test
    void sendsSortSettingsAndPlayerSortPreventionToServer() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        NewConfigOptions config = new NewConfigOptions();
        config.sortHighlightedItem = false;
        config.sortPlayerInventory = true;
        config.enableDoubleClickSort = false;
        config.sortType = SortType.MOD;
        config.preventSortForScreens.add("minecraft:anvil");
        config.preventSortForScreens.add("minecraft:chest");

        ClientConfigSync.syncConfigToServer(config, networking);

        Assertions.assertEquals(List.of(
                new SortSettings(false, true, false, SortType.MOD),
                new PlayerSortPrevention(Set.of("minecraft:anvil", "minecraft:chest"))
        ), networking.serverboundPayloads);
    }

    private static class RecordingNetworkingPlatform implements NetworkingPlatform {
        private final List<CustomPacketPayload> serverboundPayloads = new ArrayList<>();

        @Override
        public void registerPayloads() {
            throw new UnsupportedOperationException("Not needed for client config sync tests");
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
            throw new UnsupportedOperationException("Not needed for client config sync tests");
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            serverboundPayloads.add(payload);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            throw new UnsupportedOperationException("Not needed for client config sync tests");
        }
    }
}
