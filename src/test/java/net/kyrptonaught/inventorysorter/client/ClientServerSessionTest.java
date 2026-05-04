package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ClientServerSessionTest {
    @Test
    void joinResetsServerStateSendsClientSyncAndSyncsConfig() {
        ClientPacketReceivers packetReceivers = newReceivers();
        packetReceivers.applyHideButton(new HideButton(Set.of("minecraft:chest")));
        packetReceivers.markServerPresent();
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientServerSession session = new ClientServerSession(
                packetReceivers,
                networking,
                callbacks::syncConfig,
                callbacks::reloadCompatibility,
                callbacks::clearClientSortQueue
        );

        session.join(null);

        Assertions.assertFalse(packetReceivers.serverIsPresent());
        Assertions.assertTrue(packetReceivers.serverConfig().hideButtonsForScreens.isEmpty());
        Assertions.assertEquals(List.of(new ClientSync(true)), networking.serverboundPayloads);
        Assertions.assertEquals(1, callbacks.syncConfigCalls);
        Assertions.assertEquals(1, callbacks.clearClientSortQueueCalls);
        Assertions.assertTrue(session.hasPendingMissingServerWarningCheck());
    }

    @Test
    void disconnectResetsServerStateReloadsCompatibilityAndClearsPendingWarningCheck() {
        ClientPacketReceivers packetReceivers = newReceivers();
        packetReceivers.applyHideButton(new HideButton(Set.of("minecraft:chest")));
        packetReceivers.markServerPresent();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientServerSession session = new ClientServerSession(
                packetReceivers,
                new RecordingNetworkingPlatform(),
                callbacks::syncConfig,
                callbacks::reloadCompatibility,
                callbacks::clearClientSortQueue
        );
        session.join(null);

        session.disconnect();

        Assertions.assertFalse(packetReceivers.serverIsPresent());
        Assertions.assertTrue(packetReceivers.serverConfig().hideButtonsForScreens.isEmpty());
        Assertions.assertEquals(1, callbacks.reloadCompatibilityCalls);
        Assertions.assertEquals(2, callbacks.clearClientSortQueueCalls);
        Assertions.assertFalse(session.hasPendingMissingServerWarningCheck());
    }

    @Test
    void missingServerWarningWaitsForSecondCheckWhenServerIsStillMissing() {
        MutableClock clock = new MutableClock();
        ClientServerSession session = new ClientServerSession(
                newReceivers(),
                new RecordingNetworkingPlatform(),
                () -> {
                },
                () -> {
                },
                () -> {
                },
                TestConfig::new,
                "26.1.2",
                client -> Optional.of("example.org:25565"),
                client -> {
                },
                clock::now
        );
        session.join(null);

        clock.advance(5_000);
        session.tick(null);

        Assertions.assertTrue(session.hasPendingMissingServerWarningCheck());
    }

    @Test
    void missingServerWarningCheckClearsWhenServerSupportArrives() {
        ClientPacketReceivers packetReceivers = newReceivers();
        MutableClock clock = new MutableClock();
        ClientServerSession session = new ClientServerSession(
                packetReceivers,
                new RecordingNetworkingPlatform(),
                () -> {
                },
                () -> {
                },
                () -> {
                },
                TestConfig::new,
                "26.1.2",
                client -> Optional.of("example.org:25565"),
                client -> {
                },
                clock::now
        );
        session.join(null);

        packetReceivers.markServerPresent();
        clock.advance(5_000);
        session.tick(null);

        Assertions.assertFalse(session.hasPendingMissingServerWarningCheck());
    }

    @Test
    void missingServerWarningIsSuppressedAfterFirstMessageForServerAndVersion() {
        TestConfig config = new TestConfig();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientServerSession session = newSessionWithConfig(config, "26.1.2");

        Assertions.assertTrue(session.warnAboutMissingServerIfNeeded("Example.Org:25565", callbacks::showMissingServerWarning));
        Assertions.assertFalse(session.warnAboutMissingServerIfNeeded("example.org:25565", callbacks::showMissingServerWarning));

        Assertions.assertEquals(1, callbacks.missingServerWarningCalls);
        Assertions.assertEquals(1, config.saveCalls);
    }

    @Test
    void missingServerWarningIsShownAgainForNewModVersion() {
        TestConfig config = new TestConfig();
        config.markMissingServerWarningShown("example.org:25565", "26.1.2");
        RecordingCallbacks callbacks = new RecordingCallbacks();
        ClientServerSession session = newSessionWithConfig(config, "26.1.3");

        Assertions.assertTrue(session.warnAboutMissingServerIfNeeded("example.org:25565", callbacks::showMissingServerWarning));

        Assertions.assertEquals(1, callbacks.missingServerWarningCalls);
        Assertions.assertEquals(1, config.saveCalls);
    }

    private ClientServerSession newSessionWithConfig(TestConfig config, String modVersion) {
        return new ClientServerSession(
                newReceivers(),
                new RecordingNetworkingPlatform(),
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> config,
                modVersion,
                client -> Optional.of("example.org:25565"),
                client -> {
                },
                System::currentTimeMillis
        );
    }

    private ClientPacketReceivers newReceivers() {
        return new ClientPacketReceivers(
                TestConfig::new,
                () -> {
                },
                () -> {
                }
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
        private int syncConfigCalls;
        private int reloadCompatibilityCalls;
        private int clearClientSortQueueCalls;
        private int missingServerWarningCalls;

        private void syncConfig() {
            syncConfigCalls++;
        }

        private void reloadCompatibility() {
            reloadCompatibilityCalls++;
        }

        private void clearClientSortQueue() {
            clearClientSortQueueCalls++;
        }

        private void showMissingServerWarning() {
            missingServerWarningCalls++;
        }
    }

    private static class RecordingNetworkingPlatform implements NetworkingPlatform {
        private final List<CustomPacketPayload> serverboundPayloads = new ArrayList<>();

        @Override
        public void registerPayloads() {
            throw new UnsupportedOperationException("Not needed for client server session tests");
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
            throw new UnsupportedOperationException("Not needed for client server session tests");
        }

        @Override
        public void sendToServer(CustomPacketPayload payload) {
            serverboundPayloads.add(payload);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
            throw new UnsupportedOperationException("Not needed for client server session tests");
        }
    }

    private static class MutableClock {
        private long now;

        private void advance(long milliseconds) {
            now += milliseconds;
        }

        private long now() {
            return now;
        }
    }
}
