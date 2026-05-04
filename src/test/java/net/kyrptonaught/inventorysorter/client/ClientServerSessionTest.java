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
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ClientServerSessionTest {
    @Test
    void joinResetsServerStateSendsClientSyncAndSyncsConfig() {
        ClientPacketReceivers packetReceivers = newReceivers();
        packetReceivers.applyHideButton(new HideButton(Set.of("minecraft:chest")));
        packetReceivers.markServerPresent();
        RecordingNetworkingPlatform networking = new RecordingNetworkingPlatform();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        RecordingScheduledExecutorService scheduler = new RecordingScheduledExecutorService();
        ClientServerSession session = new ClientServerSession(
                packetReceivers,
                networking,
                callbacks::syncConfig,
                callbacks::reloadCompatibility,
                () -> scheduler
        );

        session.join(null);

        Assertions.assertFalse(packetReceivers.serverIsPresent());
        Assertions.assertTrue(packetReceivers.serverConfig().hideButtonsForScreens.isEmpty());
        Assertions.assertEquals(List.of(new ClientSync(true)), networking.serverboundPayloads);
        Assertions.assertEquals(1, callbacks.syncConfigCalls);
        Assertions.assertEquals(1, scheduler.scheduledTasks.size());
        Assertions.assertEquals(5, scheduler.scheduledTasks.get(0).delay);
        Assertions.assertEquals(TimeUnit.SECONDS, scheduler.scheduledTasks.get(0).unit);
    }

    @Test
    void disconnectResetsServerStateReloadsCompatibilityAndShutsDownScheduler() {
        ClientPacketReceivers packetReceivers = newReceivers();
        packetReceivers.applyHideButton(new HideButton(Set.of("minecraft:chest")));
        packetReceivers.markServerPresent();
        RecordingCallbacks callbacks = new RecordingCallbacks();
        RecordingScheduledExecutorService scheduler = new RecordingScheduledExecutorService();
        ClientServerSession session = new ClientServerSession(
                packetReceivers,
                new RecordingNetworkingPlatform(),
                callbacks::syncConfig,
                callbacks::reloadCompatibility,
                () -> scheduler
        );
        session.join(null);

        session.disconnect();

        Assertions.assertFalse(packetReceivers.serverIsPresent());
        Assertions.assertTrue(packetReceivers.serverConfig().hideButtonsForScreens.isEmpty());
        Assertions.assertEquals(1, callbacks.reloadCompatibilityCalls);
        Assertions.assertTrue(scheduler.shutdown);
        Assertions.assertEquals(1, scheduler.awaitTerminationCalls);
        Assertions.assertFalse(scheduler.shutdownNow);
    }

    @Test
    void shutdownForcesSchedulerShutdownWhenAwaitTimesOut() {
        RecordingScheduledExecutorService scheduler = new RecordingScheduledExecutorService();
        scheduler.awaitTerminationResult = false;
        ClientServerSession session = new ClientServerSession(
                newReceivers(),
                new RecordingNetworkingPlatform(),
                () -> {
                },
                () -> {
                },
                () -> scheduler
        );
        session.join(null);

        session.shutdown();

        Assertions.assertTrue(scheduler.shutdown);
        Assertions.assertTrue(scheduler.shutdownNow);
    }

    @Test
    void missingServerWarningSchedulesSecondCheckWhenServerIsStillMissing() {
        RecordingScheduledExecutorService scheduler = new RecordingScheduledExecutorService();
        ClientServerSession session = new ClientServerSession(
                newReceivers(),
                new RecordingNetworkingPlatform(),
                () -> {
                },
                () -> {
                },
                () -> scheduler
        );
        session.join(null);

        scheduler.scheduledTasks.get(0).command.run();

        Assertions.assertEquals(2, scheduler.scheduledTasks.size());
        Assertions.assertEquals(20, scheduler.scheduledTasks.get(1).delay);
        Assertions.assertEquals(TimeUnit.SECONDS, scheduler.scheduledTasks.get(1).unit);
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
        @Override
        public void save() {
        }
    }

    private static class RecordingCallbacks {
        private int syncConfigCalls;
        private int reloadCompatibilityCalls;

        private void syncConfig() {
            syncConfigCalls++;
        }

        private void reloadCompatibility() {
            reloadCompatibilityCalls++;
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

    private static class RecordingScheduledExecutorService extends AbstractExecutorService implements ScheduledExecutorService {
        private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
        private boolean shutdown;
        private boolean shutdownNow;
        private int awaitTerminationCalls;
        private boolean awaitTerminationResult = true;

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            scheduledTasks.add(new ScheduledTask(command, delay, unit));
            return new CompletedScheduledFuture();
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException("Not needed for client server session tests");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            throw new UnsupportedOperationException("Not needed for client server session tests");
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException("Not needed for client server session tests");
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdownNow = true;
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            awaitTerminationCalls++;
            return awaitTerminationResult;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private record ScheduledTask(Runnable command, long delay, TimeUnit unit) {
    }

    private static class CompletedScheduledFuture implements ScheduledFuture<Object> {
        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }
}
