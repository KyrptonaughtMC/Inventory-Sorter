package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.sort.plan.ClientFallbackSortPlanBuilder;
import net.kyrptonaught.inventorysorter.client.sort.plan.ClientSortClickPlanner;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.world.inventory.ContainerInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class ClientSideInventorySorterTest {
    @Test
    void optOutRejectsDirectPlanAndLeavesContainerSortAvailable() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor(() -> false);
        Assertions.assertFalse(ClientSideInventorySorter.enqueueSortPlans(SortTarget.PLAYER_INVENTORY, () -> true, () -> false,
                target -> { throw new AssertionError("Disabled player sort must not be planned"); }, executor));
        Assertions.assertTrue(ClientSideInventorySorter.enqueueSortPlans(SortTarget.CONTAINER, () -> true, () -> false,
                target -> {
                    Assertions.assertEquals(SortTarget.CONTAINER, target);
                    return Optional.of(sort(7, 1));
                }, executor));
        Assertions.assertEquals(1, executor.pendingSortCount());
    }

    @Test
    void queuedOptOutDropsPlayerPlanButKeepsContainerPlan() {
        boolean[] allow = {true};
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor(() -> allow[0]);
        executor.replacePendingSorts(List.of(
                new ClientInventoryClickExecutor.QueuedSort(7, sort(7, 1).clicks(), SortTarget.PLAYER_INVENTORY),
                new ClientInventoryClickExecutor.QueuedSort(7, sort(7, 2).clicks(), SortTarget.CONTAINER)));
        allow[0] = false;
        java.util.ArrayList<Integer> slots = new java.util.ArrayList<>();
        executor.tick(new ClientInventoryClickExecutor.ClickSender() {
            public boolean canSend(int menuId) { return true; }
            public void send(PlannedContainerClick click) { slots.add(click.slotIndex()); }
        });
        Assertions.assertEquals(List.of(2), slots);
        Assertions.assertEquals(0, executor.pendingSortCount());
    }
    @Test
    void containerSortQueuesPlayerInventoryPlanWhenConfigured() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();

        boolean accepted = ClientSideInventorySorter.enqueueSortPlans(
                SortTarget.CONTAINER,
                () -> true,
                target -> Optional.of(sort(7, target.ordinal())),
                executor
        );

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(2, executor.pendingSortCount());
    }

    @Test
    void containerSortStillQueuesContainerPlanWhenConfiguredPlayerPlanCannotBeBuilt() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();

        boolean accepted = ClientSideInventorySorter.enqueueSortPlans(
                SortTarget.CONTAINER,
                () -> true,
                target -> target == SortTarget.CONTAINER ? Optional.of(sort(7, 1)) : Optional.empty(),
                executor
        );

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(1, executor.pendingSortCount());
    }

    @Test
    void rejectedPrimaryPlanDoesNotReplacePendingWork() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        executor.replacePendingSorts(List.of(sort(7, 1)));

        boolean accepted = ClientSideInventorySorter.enqueueSortPlans(
                SortTarget.CONTAINER,
                () -> true,
                target -> Optional.empty(),
                executor
        );

        Assertions.assertFalse(accepted);
        Assertions.assertEquals(1, executor.pendingSortCount());
    }

    @Test
    void playerInventorySortDoesNotQueueAdditionalPlayerInventoryPlan() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();

        boolean accepted = ClientSideInventorySorter.enqueueSortPlans(
                SortTarget.PLAYER_INVENTORY,
                () -> true,
                target -> Optional.of(sort(7, target.ordinal())),
                executor
        );

        Assertions.assertTrue(accepted);
        Assertions.assertEquals(1, executor.pendingSortCount());
    }

    @Test
    void currentScreenSortIsRejectedWhenMinecraftStateCannotResolveScope() {
        ClientInventoryClickExecutor executor = new ClientInventoryClickExecutor();
        ClientSideInventorySorter sorter = new ClientSideInventorySorter(
                () -> null,
                () -> "en_us",
                () -> SortType.NAME,
                List::of,
                () -> true,
                () -> true,
                () -> true,
                () -> true,
                executor,
                new ClientFallbackSortPlanBuilder(new ClientSortClickPlanner())
        );

        Assertions.assertFalse(sorter.enqueueCurrentScreenSort(SortTarget.CONTAINER));
        Assertions.assertEquals(0, executor.pendingSortCount());
    }

    private static ClientInventoryClickExecutor.QueuedSort sort(int menuId, int slot) {
        return new ClientInventoryClickExecutor.QueuedSort(
                menuId,
                List.of(new PlannedContainerClick(slot, 0, ContainerInput.PICKUP))
        );
    }
}
