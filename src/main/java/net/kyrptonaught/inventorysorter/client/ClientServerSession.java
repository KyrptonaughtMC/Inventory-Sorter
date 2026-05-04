package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public class ClientServerSession {
    private final ClientPacketReceivers packetReceivers;
    private final NetworkingPlatform networking;
    private final Runnable syncConfig;
    private final Runnable reloadCompatibility;
    private final Supplier<ScheduledExecutorService> schedulerFactory;
    private ScheduledExecutorService scheduler;

    public ClientServerSession(ClientPacketReceivers packetReceivers) {
        this(
                packetReceivers,
                PlatformServices.NETWORK,
                InventorySorterModClient::syncConfig,
                compatibility::reload,
                Executors::newSingleThreadScheduledExecutor
        );
    }

    ClientServerSession(
            ClientPacketReceivers packetReceivers,
            NetworkingPlatform networking,
            Runnable syncConfig,
            Runnable reloadCompatibility,
            Supplier<ScheduledExecutorService> schedulerFactory
    ) {
        this.packetReceivers = packetReceivers;
        this.networking = networking;
        this.syncConfig = syncConfig;
        this.reloadCompatibility = reloadCompatibility;
        this.schedulerFactory = schedulerFactory;
    }

    public void join(Minecraft client) {
        packetReceivers.resetServerState();
        scheduler = schedulerFactory.get();

        networking.sendToServer(new ClientSync(true));
        syncConfig.run();

        scheduleMissingServerWarning(client);
    }

    public void disconnect() {
        packetReceivers.resetServerState();
        reloadCompatibility.run();
        shutdown();
    }

    public void shutdown() {
        if (scheduler == null || scheduler.isShutdown()) return;

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void scheduleMissingServerWarning(Minecraft client) {
        // Two-stage check: first at 5 seconds, then at 25 seconds if still no server
        scheduler.schedule(() -> {
            if (!packetReceivers.serverIsPresent()) {
                // First check at 5 seconds - schedule another check at 25 seconds
                scheduler.schedule(() -> {
                    if (!packetReceivers.serverIsPresent() && client != null && client.player != null) {
                        client.execute(() -> client.player.sendSystemMessage(
                                Component.literal("[Inventory Sorter] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA))
                                        .append(Component.translatable("inventorysorter.warning.missing-server").withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                                        )));
                    }
                }, 20, TimeUnit.SECONDS);
            }
        }, 5, TimeUnit.SECONDS);
    }
}
