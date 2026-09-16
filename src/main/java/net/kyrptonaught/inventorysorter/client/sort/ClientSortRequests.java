package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.ClientServerSupport;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;

import java.util.function.BooleanSupplier;

class ClientSortRequests {
    private final ClientServerSupport serverSupport;
    private final ServerSortSender serverSortSender;
    private final ClientFallbackSorter clientFallbackSorter;
    private final BooleanSupplier allowPlayerInventorySorting;

    ClientSortRequests(
            ClientServerSupport serverSupport,
            ServerSortSender serverSortSender,
            ClientFallbackSorter clientFallbackSorter
    ) {
        this(serverSupport, serverSortSender, clientFallbackSorter, () -> true);
    }

    ClientSortRequests(ClientServerSupport serverSupport, ServerSortSender serverSortSender, ClientFallbackSorter clientFallbackSorter,
                       BooleanSupplier allowPlayerInventorySorting) {
        this.serverSupport = serverSupport;
        this.serverSortSender = serverSortSender;
        this.clientFallbackSorter = clientFallbackSorter;
        this.allowPlayerInventorySorting = allowPlayerInventorySorting;
    }

    boolean requestSort(SortTarget target) {
        if (!SortabilityPolicy.isTargetAllowed(target, allowPlayerInventorySorting.getAsBoolean())) {
            return false;
        }
        if (serverSupport.shouldUseServerSorting()) {
            serverSortSender.sendSortPacket(target);
            return true;
        }

        return clientFallbackSorter.enqueueCurrentScreenSort(target);
    }

    interface ServerSortSender {
        void sendSortPacket(SortTarget target);
    }

    interface ClientFallbackSorter {
        boolean enqueueCurrentScreenSort(SortTarget target);
    }
}
