package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.kyrptonaught.inventorysorter.client.sort.ClientSortScope;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.minecraft.world.inventory.ContainerInput;

import java.util.ArrayList;
import java.util.List;

final class BundleInsertionClickAdapter {
    List<PlannedContainerClick> clicks(ClientSortScope scope, BundleInsertionLayoutPass.Result bundleInsertion) {
        List<PlannedContainerClick> clicks = new ArrayList<>();
        for (BundleInsertionLayoutPass.BundleInsertion insertion : bundleInsertion.insertions()) {
            clicks.add(pickupClick(scope.slots().get(insertion.sourceLayoutIndex()).menuSlotIndex()));
            for (BundleInsertionLayoutPass.BundleInsertionTarget target : insertion.targets()) {
                clicks.add(pickupClick(targetMenuSlotIndex(scope, target)));
            }
            if (!bundleInsertion.layoutStacks().get(insertion.sourceLayoutIndex()).isEmpty()) {
                clicks.add(pickupClick(scope.slots().get(insertion.sourceLayoutIndex()).menuSlotIndex()));
            }
        }
        return clicks;
    }

    private static int targetMenuSlotIndex(ClientSortScope scope, BundleInsertionLayoutPass.BundleInsertionTarget target) {
        return switch (target.area()) {
            case LAYOUT -> scope.slots().get(target.index()).menuSlotIndex();
            case EXTRA_TARGET -> scope.hotbarBundleTargetSlots().get(target.index()).menuSlotIndex();
        };
    }

    private static PlannedContainerClick pickupClick(int slotIndex) {
        return new PlannedContainerClick(slotIndex, 0, ContainerInput.PICKUP);
    }
}
