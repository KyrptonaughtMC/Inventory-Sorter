package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.client.sort.ClientSortRuntime;
import net.kyrptonaught.inventorysorter.client.sort.ClientSorts;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.client.Minecraft;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

/**
 * Client-domain runtime composed outside the Fabric entry point.
 *
 * The Fabric initializer creates one runtime and wires loader events to the objects exposed here,
 * keeping {@link InventorySorterModClient} focused on loader registration.
 */
public class InventorySorterClientRuntime {
    private final ClientSortRuntime clientSortRuntime;
    private final ClientPacketReceivers clientPacketReceivers;
    private final ClientServerSession clientServerSession;

    private InventorySorterClientRuntime(
            ClientSortRuntime clientSortRuntime,
            ClientPacketReceivers clientPacketReceivers,
            ClientServerSession clientServerSession
    ) {
        this.clientSortRuntime = clientSortRuntime;
        this.clientPacketReceivers = clientPacketReceivers;
        this.clientServerSession = clientServerSession;
    }

    public static InventorySorterClientRuntime create() {
        ClientSortRuntime clientSortRuntime = ClientSortRuntime.create(
                Minecraft::getInstance,
                InventorySorterClientRuntime::languageCode,
                () -> getConfig().sortType,
                () -> getConfig().sortPriorityRules,
                () -> getConfig().sortPlayerInventory
        );
        ClientSorts.configure(clientSortRuntime);

        ClientPacketReceivers clientPacketReceivers = new ClientPacketReceivers(
                InventorySorterMod::getConfig,
                InventorySorterMod::reloadConfig,
                compatibility::reload,
                clientSortRuntime.serverSupport()
        );
        ClientServerSession clientServerSession = new ClientServerSession(
                clientPacketReceivers,
                PlatformServices.NETWORK,
                ClientConfigSync::syncConfigToServer,
                compatibility::reload,
                clientSortRuntime::clearPendingClicks
        );

        return new InventorySorterClientRuntime(clientSortRuntime, clientPacketReceivers, clientServerSession);
    }

    public ClientSortRuntime clientSortRuntime() {
        return clientSortRuntime;
    }

    public ClientPacketReceivers clientPacketReceivers() {
        return clientPacketReceivers;
    }

    public ClientServerSession clientServerSession() {
        return clientServerSession;
    }

    private static String languageCode() {
        return Minecraft.getInstance().options.languageCode;
    }
}
