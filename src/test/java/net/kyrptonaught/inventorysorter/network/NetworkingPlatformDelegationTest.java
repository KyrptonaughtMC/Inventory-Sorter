package net.kyrptonaught.inventorysorter.network;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkingPlatformDelegationTest {
    @Test
    void playerboundPacketHelpersUseNetworkingPlatform() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();

        SortSettings.DEFAULT.sync(null, networking);
        PlayerSortPrevention.DEFAULT.sync(null, networking);
        HideButton.DEFAULT.sync(null, networking);
        new ReloadConfigPacket().fire(null, networking);
        LastSeenVersionPacket.DEFAULT.send(null, networking);

        Assertions.assertEquals(List.of(
                SortSettings.DEFAULT,
                PlayerSortPrevention.DEFAULT,
                HideButton.DEFAULT,
                new ReloadConfigPacket(),
                LastSeenVersionPacket.DEFAULT
        ), networking.playerboundPayloads);
    }

    @Test
    void sortPacketSendsConfiguredSortTypeToServer() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        NewConfigOptions config = new NewConfigOptions();
        config.sortType = SortType.CATEGORY;

        InventorySortPacket.sendSortPacket(SortTarget.PLAYER_INVENTORY, config, networking);

        Assertions.assertEquals(List.of(new InventorySortPacket(SortTarget.PLAYER_INVENTORY, SortType.CATEGORY)), networking.serverboundPayloads);
    }

    @Test
    void sortPacketAlsoSendsPlayerInventoryWhenEnabled() {
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        NewConfigOptions config = new NewConfigOptions();
        config.sortPlayerInventory = true;

        InventorySortPacket.sendSortPacket(SortTarget.CONTAINER, config, networking);

        Assertions.assertEquals(List.of(
                new InventorySortPacket(SortTarget.CONTAINER, SortType.NAME),
                new InventorySortPacket(SortTarget.PLAYER_INVENTORY, SortType.NAME)
        ), networking.serverboundPayloads);
    }

    private static class RecordingNetworkingPlatform implements NetworkingPlatform {
        private final List<CustomPacketPayload> serverboundPayloads = new ArrayList<>();
        private final List<CustomPacketPayload> playerboundPayloads = new ArrayList<>();

        @Override
        public void registerPayloads() {
            throw new UnsupportedOperationException("Not needed for packet send tests");
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
            throw new UnsupportedOperationException("Not needed for packet send tests");
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            serverboundPayloads.add(payload);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            playerboundPayloads.add(payload);
        }
    }
}
