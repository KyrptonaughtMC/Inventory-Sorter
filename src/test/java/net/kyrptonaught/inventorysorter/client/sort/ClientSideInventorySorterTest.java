package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.client.sort.plan.PlannedContainerClick;
import net.minecraft.world.inventory.ContainerInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class ClientSideInventorySorterTest {
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

    private static ClientInventoryClickExecutor.QueuedSort sort(int menuId, int slot) {
        return new ClientInventoryClickExecutor.QueuedSort(
                menuId,
                List.of(new PlannedContainerClick(slot, 0, ContainerInput.PICKUP))
        );
    }
}
