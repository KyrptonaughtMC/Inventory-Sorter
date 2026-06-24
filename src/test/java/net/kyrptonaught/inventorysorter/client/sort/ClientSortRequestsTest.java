package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.ClientServerSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientSortRequestsTest {
    @Test
    void unknownServerSupportUsesFallback() {
        RecordingServerSortSender serverSortSender = new RecordingServerSortSender();
        RecordingFallbackSorter fallbackSorter = new RecordingFallbackSorter(true);
        ClientSortRequests requests = new ClientSortRequests(
                new ClientServerSupport(),
                serverSortSender,
                fallbackSorter
        );

        boolean accepted = requests.requestSort(SortTarget.PLAYER_INVENTORY);

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(0, serverSortSender.calls);
        Assertions.assertEquals(1, fallbackSorter.calls);
        Assertions.assertEquals(SortTarget.PLAYER_INVENTORY, fallbackSorter.lastTarget);
    }

    @Test
    void presentServerSupportSendsServerPacket() {
        ClientServerSupport serverSupport = new ClientServerSupport();
        serverSupport.markPresent();
        RecordingServerSortSender serverSortSender = new RecordingServerSortSender();
        RecordingFallbackSorter fallbackSorter = new RecordingFallbackSorter(true);
        ClientSortRequests requests = new ClientSortRequests(serverSupport, serverSortSender, fallbackSorter);

        boolean accepted = requests.requestSort(SortTarget.CONTAINER);

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(1, serverSortSender.calls);
        Assertions.assertEquals(SortTarget.CONTAINER, serverSortSender.lastTarget);
        Assertions.assertEquals(0, fallbackSorter.calls);
    }

    @Test
    void switchesFromFallbackToServerPacketWhenPresenceArrives() {
        ClientServerSupport serverSupport = new ClientServerSupport();
        RecordingServerSortSender serverSortSender = new RecordingServerSortSender();
        RecordingFallbackSorter fallbackSorter = new RecordingFallbackSorter(true);
        ClientSortRequests requests = new ClientSortRequests(serverSupport, serverSortSender, fallbackSorter);

        Assertions.assertTrue(requests.requestSort(SortTarget.CONTAINER));

        serverSupport.markPresent();

        Assertions.assertTrue(requests.requestSort(SortTarget.PLAYER_INVENTORY));
        Assertions.assertEquals(1, fallbackSorter.calls);
        Assertions.assertEquals(SortTarget.CONTAINER, fallbackSorter.lastTarget);
        Assertions.assertEquals(1, serverSortSender.calls);
        Assertions.assertEquals(SortTarget.PLAYER_INVENTORY, serverSortSender.lastTarget);
    }

    @Test
    void absentServerSupportUsesFallback() {
        ClientServerSupport serverSupport = new ClientServerSupport();
        serverSupport.markAbsent();
        RecordingServerSortSender serverSortSender = new RecordingServerSortSender();
        RecordingFallbackSorter fallbackSorter = new RecordingFallbackSorter(true);
        ClientSortRequests requests = new ClientSortRequests(serverSupport, serverSortSender, fallbackSorter);

        boolean accepted = requests.requestSort(SortTarget.PLAYER_INVENTORY);

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(0, serverSortSender.calls);
        Assertions.assertEquals(1, fallbackSorter.calls);
        Assertions.assertEquals(SortTarget.PLAYER_INVENTORY, fallbackSorter.lastTarget);
    }

    @Test
    void absentServerSupportReportsFallbackRejection() {
        ClientServerSupport serverSupport = new ClientServerSupport();
        serverSupport.markAbsent();
        ClientSortRequests requests = new ClientSortRequests(
                serverSupport,
                new RecordingServerSortSender(),
                new RecordingFallbackSorter(false)
        );

        Assertions.assertFalse(requests.requestSort(SortTarget.PLAYER_INVENTORY));
    }

    private static class RecordingServerSortSender implements ClientSortRequests.ServerSortSender {
        private int calls;
        private SortTarget lastTarget;

        @Override
        public void sendSortPacket(SortTarget target) {
            calls++;
            lastTarget = target;
        }
    }

    private static class RecordingFallbackSorter implements ClientSortRequests.ClientFallbackSorter {
        private final boolean result;
        private int calls;
        private SortTarget lastTarget;

        private RecordingFallbackSorter(boolean result) {
            this.result = result;
        }

        @Override
        public boolean enqueueCurrentScreenSort(SortTarget target) {
            calls++;
            lastTarget = target;
            return result;
        }
    }
}
