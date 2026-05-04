package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.VERSION;

public class ClientServerSession {
    private final ClientPacketReceivers packetReceivers;
    private final NetworkingPlatform networking;
    private final Runnable syncConfig;
    private final Runnable reloadCompatibility;
    private final Runnable clearClientSortQueue;
    private final Supplier<NewConfigOptions> config;
    private final String modVersion;
    private final Function<Minecraft, Optional<String>> serverAddress;
    private final Consumer<Minecraft> missingServerWarning;
    private final LongSupplier currentTimeMillis;
    private long missingServerWarningCheckAtMillis = NO_PENDING_MISSING_SERVER_CHECK;
    private boolean missingServerWarningFirstCheckDone;
    private static final long NO_PENDING_MISSING_SERVER_CHECK = -1;
    private static final long FIRST_MISSING_SERVER_CHECK_DELAY_MS = 5_000;
    private static final long SECOND_MISSING_SERVER_CHECK_DELAY_MS = 20_000;

    ClientServerSession(
            ClientPacketReceivers packetReceivers,
            NetworkingPlatform networking,
            Runnable syncConfig,
            Runnable reloadCompatibility,
            Runnable clearClientSortQueue
    ) {
        this(
                packetReceivers,
                networking,
                syncConfig,
                reloadCompatibility,
                clearClientSortQueue,
                InventorySorterMod::getConfig,
                VERSION,
                ClientServerSession::currentServerAddress,
                ClientServerSession::sendMissingServerWarning,
                System::currentTimeMillis
        );
    }

    ClientServerSession(
            ClientPacketReceivers packetReceivers,
            NetworkingPlatform networking,
            Runnable syncConfig,
            Runnable reloadCompatibility,
            Runnable clearClientSortQueue,
            Supplier<NewConfigOptions> config,
            String modVersion,
            Function<Minecraft, Optional<String>> serverAddress,
            Consumer<Minecraft> missingServerWarning,
            LongSupplier currentTimeMillis
    ) {
        this.packetReceivers = packetReceivers;
        this.networking = networking;
        this.syncConfig = syncConfig;
        this.reloadCompatibility = reloadCompatibility;
        this.clearClientSortQueue = clearClientSortQueue;
        this.config = config;
        this.modVersion = modVersion;
        this.serverAddress = serverAddress;
        this.missingServerWarning = missingServerWarning;
        this.currentTimeMillis = currentTimeMillis;
    }

    public void join(Minecraft client) {
        packetReceivers.resetServerState();
        clearClientSortQueue.run();
        missingServerWarningFirstCheckDone = false;
        missingServerWarningCheckAtMillis = currentTimeMillis.getAsLong() + FIRST_MISSING_SERVER_CHECK_DELAY_MS;

        networking.sendToServer(new ClientSync(true));
        syncConfig.run();
    }

    public void disconnect() {
        packetReceivers.resetServerState();
        clearClientSortQueue.run();
        reloadCompatibility.run();
        clearMissingServerWarningCheck();
    }

    public void tick(Minecraft client) {
        if (missingServerWarningCheckAtMillis == NO_PENDING_MISSING_SERVER_CHECK) {
            return;
        }
        if (packetReceivers.serverIsPresent()) {
            clearMissingServerWarningCheck();
            return;
        }
        if (currentTimeMillis.getAsLong() < missingServerWarningCheckAtMillis) {
            return;
        }
        if (!missingServerWarningFirstCheckDone) {
            missingServerWarningFirstCheckDone = true;
            missingServerWarningCheckAtMillis = currentTimeMillis.getAsLong() + SECOND_MISSING_SERVER_CHECK_DELAY_MS;
            return;
        }
        if (client == null || client.player == null) {
            return;
        }

        packetReceivers.markServerAbsent();
        clearMissingServerWarningCheck();
        warnAboutMissingServerIfNeeded(client);
    }

    boolean hasPendingMissingServerWarningCheck() {
        return missingServerWarningCheckAtMillis != NO_PENDING_MISSING_SERVER_CHECK;
    }

    private void clearMissingServerWarningCheck() {
        missingServerWarningCheckAtMillis = NO_PENDING_MISSING_SERVER_CHECK;
        missingServerWarningFirstCheckDone = false;
    }

    private void warnAboutMissingServerIfNeeded(Minecraft client) {
        Optional<String> currentServerAddress = serverAddress.apply(client);
        if (currentServerAddress.isEmpty()) {
            return;
        }

        warnAboutMissingServerIfNeeded(
                currentServerAddress.get(),
                () -> missingServerWarning.accept(client)
        );
    }

    boolean warnAboutMissingServerIfNeeded(String currentServerAddress, Runnable sendWarning) {
        NewConfigOptions currentConfig = config.get();
        if (!currentConfig.shouldShowMissingServerWarning(currentServerAddress, modVersion)) {
            return false;
        }

        currentConfig.markMissingServerWarningShown(currentServerAddress, modVersion);
        currentConfig.save();
        sendWarning.run();
        return true;
    }

    private static Optional<String> currentServerAddress(Minecraft client) {
        if (client == null) {
            return Optional.empty();
        }

        ServerData serverData = client.getCurrentServer();
        if (serverData == null || serverData.ip == null || serverData.ip.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(serverData.ip);
    }

    private static void sendMissingServerWarning(Minecraft client) {
        client.player.sendSystemMessage(
                Component.literal("[Inventory Sorter] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA))
                        .append(Component.translatable("inventorysorter.warning.missing-server").withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                        ));
    }
}
