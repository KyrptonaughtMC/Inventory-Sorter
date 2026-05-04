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

public class ClientPacketReceiversTest {
    @Test
    void productionConstructorRegistersClientReceivers() {
        ClientPacketReceivers receivers = new ClientPacketReceivers();
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();

        receivers.register(networking);

        Assertions.assertNotNull(networking.sortSettingsHandler);
        Assertions.assertNotNull(networking.playerSortPreventionHandler);
        Assertions.assertNotNull(networking.hideButtonHandler);
        Assertions.assertNotNull(networking.reloadConfigHandler);
        Assertions.assertNotNull(networking.lastSeenVersionHandler);
        Assertions.assertNotNull(networking.serverPresenceHandler);
    }

    @Test
    void registerWiresAllClientReceivers() {
        TestConfig config = new TestConfig();
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientPacketReceivers receivers = new ClientPacketReceivers(
                () -> config,
                callbacks::reloadConfig,
                callbacks::reloadCompatibility
        );

        receivers.register(networking);
        networking.sortSettingsHandler.accept(new SortSettings(false, false, false, SortType.MOD));
        networking.playerSortPreventionHandler.accept(new PlayerSortPrevention(Set.of("minecraft:anvil")));
        networking.hideButtonHandler.accept(new HideButton(Set.of("minecraft:chest")));
        networking.reloadConfigHandler.run();
        networking.serverPresenceHandler.run();

        Assertions.assertEquals(SortType.MOD, config.sortType);
        Assertions.assertEquals(List.of("minecraft:anvil"), config.preventSortForScreens);
        Assertions.assertEquals(List.of("minecraft:chest"), receivers.serverConfig().hideButtonsForScreens);
        Assertions.assertEquals(1, callbacks.reloadConfigCalls);
        Assertions.assertEquals(2, callbacks.reloadCompatibilityCalls);
        Assertions.assertNotNull(networking.lastSeenVersionHandler);
        Assertions.assertTrue(receivers.serverIsPresent());
    }

    @Test
    void sortSettingsUpdateClientConfigAndSave() {
        TestConfig config = new TestConfig();
        ClientPacketReceivers receivers = newReceivers(config);

        receivers.applySortSettings(new SortSettings(false, true, false, SortType.CATEGORY));

        Assertions.assertFalse(config.enableDoubleClickSort);
        Assertions.assertEquals(SortType.CATEGORY, config.sortType);
        Assertions.assertEquals(1, config.saveCalls);
    }

    @Test
    void playerSortPreventionReplacesClientPreventionListAndReloadsCompatibility() {
        TestConfig config = new TestConfig();
        config.preventSortForScreens.add("minecraft:anvil");
        config.preventSortForScreens.add("minecraft:blast_furnace");
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientPacketReceivers receivers = new ClientPacketReceivers(
                () -> config,
                callbacks::reloadConfig,
                callbacks::reloadCompatibility
        );

        receivers.applyPlayerSortPrevention(new PlayerSortPrevention(Set.of("minecraft:anvil", "minecraft:chest")));

        Assertions.assertEquals(Set.of("minecraft:anvil", "minecraft:chest"), Set.copyOf(config.preventSortForScreens));
        Assertions.assertEquals(1, config.saveCalls);
        Assertions.assertEquals(1, callbacks.reloadCompatibilityCalls);
    }

    @Test
    void hideButtonUpdatesServerConfigAndReloadsCompatibility() {
        TestConfig config = new TestConfig();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientPacketReceivers receivers = new ClientPacketReceivers(
                () -> config,
                callbacks::reloadConfig,
                callbacks::reloadCompatibility
        );

        receivers.applyHideButton(new HideButton(Set.of("minecraft:chest", "minecraft:anvil")));

        Assertions.assertEquals(Set.of("minecraft:chest", "minecraft:anvil"), Set.copyOf(receivers.serverConfig().hideButtonsForScreens));
        Assertions.assertEquals(1, callbacks.reloadCompatibilityCalls);
    }

    @Test
    void resetServerStateClearsServerProvidedStateAndPresence() {
        TestConfig config = new TestConfig();
        ClientPacketReceivers receivers = newReceivers(config);
        receivers.applyHideButton(new HideButton(Set.of("minecraft:chest")));
        receivers.markServerPresent();

        receivers.resetServerState();

        Assertions.assertTrue(receivers.serverConfig().hideButtonsForScreens.isEmpty());
        Assertions.assertFalse(receivers.serverIsPresent());
    }

    private ClientPacketReceivers newReceivers(TestConfig config) {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        return new ClientPacketReceivers(
                () -> config,
                callbacks::reloadConfig,
                callbacks::reloadCompatibility
        );
    }

    private static class TestConfig extends NewConfigOptions {
        private int saveCalls;

        @Override
        public void save() {
            saveCalls++;
        }
    }

    private static class RecordingCallbacks {
        private int reloadConfigCalls;
        private int reloadCompatibilityCalls;

        private void reloadConfig() {
            reloadConfigCalls++;
        }

        private void reloadCompatibility() {
            reloadCompatibilityCalls++;
        }

    }

    private static class RecordingNetworkingPlatform implements NetworkingPlatform {
        private Consumer<SortSettings> sortSettingsHandler;
        private Consumer<PlayerSortPrevention> playerSortPreventionHandler;
        private Consumer<HideButton> hideButtonHandler;
        private Runnable reloadConfigHandler;
        private Consumer<LastSeenVersionPacket> lastSeenVersionHandler;
        private Runnable serverPresenceHandler;

        @Override
        public void registerPayloads() {
            throw new UnsupportedOperationException("Not needed for client receiver tests");
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
            this.sortSettingsHandler = sortSettingsHandler;
            this.playerSortPreventionHandler = playerSortPreventionHandler;
            this.hideButtonHandler = hideButtonHandler;
            this.reloadConfigHandler = reloadConfigHandler;
            this.lastSeenVersionHandler = lastSeenVersionHandler;
            this.serverPresenceHandler = serverPresenceHandler;
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            throw new UnsupportedOperationException("Not needed for client receiver tests");
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            throw new UnsupportedOperationException("Not needed for client receiver tests");
        }
    }
}
