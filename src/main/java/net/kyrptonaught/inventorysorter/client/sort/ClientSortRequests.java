package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.ClientServerSupport;

class ClientSortRequests {
    private final ClientServerSupport serverSupport;
    private final ServerSortSender serverSortSender;
    private final ClientFallbackSorter clientFallbackSorter;

    ClientSortRequests(
            ClientServerSupport serverSupport,
            ServerSortSender serverSortSender,
            ClientFallbackSorter clientFallbackSorter
    ) {
        this.serverSupport = serverSupport;
        this.serverSortSender = serverSortSender;
        this.clientFallbackSorter = clientFallbackSorter;
    }

    boolean requestSort(SortTarget target) {
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
