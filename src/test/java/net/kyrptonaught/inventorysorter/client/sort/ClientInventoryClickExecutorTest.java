package net.kyrptonaught.inventorysorter.client.sort;

import net.minecraft.world.inventory.ContainerInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ClientInventoryClickExecutorTest {
    @Test
    void tickSendsQueuedClicksInOneBurst() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        RecordingClickSender sender = new RecordingClickSender(7);
        executor.replacePendingSorts(List.of(sort(7, click(1), click(2))));

        Assertions.assertTrue(executor.tick(sender));
        Assertions.assertEquals(List.of(click(1), click(2)), sender.sentClicks);
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    @Test
    void tickClearsQueueWhenMenuChanges() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        RecordingClickSender sender = new RecordingClickSender(8);
        executor.replacePendingSorts(List.of(sort(7, click(1), click(2))));

        Assertions.assertFalse(executor.tick(sender));

        Assertions.assertTrue(sender.sentClicks.isEmpty());
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    @Test
    void tickStopsBurstWhenMenuChangesDuringExecution() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        RecordingClickSender sender = new RecordingClickSender(7);
        sender.menuChangesAfterSentClicks = 1;
        executor.replacePendingSorts(List.of(sort(7, click(1), click(2))));

        Assertions.assertTrue(executor.tick(sender));

        Assertions.assertEquals(List.of(click(1)), sender.sentClicks);
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    @Test
    void emptyClickPlansAreIgnored() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();

        executor.replacePendingSorts(List.of(sort(7)));

        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    @Test
    void newPlansReplacePendingStalePlans() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        RecordingClickSender sender = new RecordingClickSender(7);
        executor.replacePendingSorts(List.of(sort(7, click(1), click(2))));

        executor.replacePendingSorts(List.of(sort(7, click(3))));

        Assertions.assertTrue(executor.tick(sender));
        Assertions.assertEquals(List.of(click(3)), sender.sentClicks);
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    @Test
    void replacementCanContainMultiplePlansForTheSameRequest() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        RecordingClickSender sender = new RecordingClickSender(7);
        executor.replacePendingSorts(List.of(
                sort(7, click(1)),
                sort(7, click(2))
        ));

        Assertions.assertTrue(executor.tick(sender));
        Assertions.assertEquals(List.of(click(1), click(2)), sender.sentClicks);
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    private static ClientInventoryClickExecutor.QueuedSort sort(
            int menuId,
            ClientSortClickPlanner.PlannedContainerClick... clicks
    ) {
        return new ClientInventoryClickExecutor.QueuedSort(menuId, List.of(clicks));
    }

    private static ClientSortClickPlanner.PlannedContainerClick click(int slot) {
        return new ClientSortClickPlanner.PlannedContainerClick(slot, 0, ContainerInput.PICKUP);
    }

    private static class RecordingClickSender implements ClientInventoryClickExecutor.ClickSender {
        private final int currentMenuId;
        private final List<ClientSortClickPlanner.PlannedContainerClick> sentClicks = new ArrayList<>();
        private int menuChangesAfterSentClicks = Integer.MAX_VALUE;

        private RecordingClickSender(int currentMenuId) {
            this.currentMenuId = currentMenuId;
        }

        @Override
        public boolean canSend(int menuId) {
            return currentMenuId == menuId && sentClicks.size() < menuChangesAfterSentClicks;
        }

        @Override
        public void send(ClientSortClickPlanner.PlannedContainerClick click) {
            sentClicks.add(click);
        }
    }
}
