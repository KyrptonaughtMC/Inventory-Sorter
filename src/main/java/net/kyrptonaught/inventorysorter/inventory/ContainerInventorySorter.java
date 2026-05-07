package net.kyrptonaught.inventorysorter.inventory;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.inventory.container.ContainerStacks;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class ContainerInventorySorter {
    private ContainerInventorySorter() {
    }

    public static void sort(
            Container container,
            int startSlot,
            int size,
            SortType sortType,
            String languageCode,
            List<SortPriorityRuleSetting> sortPriorityRules,
            boolean sortIntoBundles
    ) {
        List<ItemStack> stacks = ContainerStacks.get(container, startSlot, size);

        SortedInventoryLayout sortedInventoryLayout = SortedInventoryLayout.from(
                stacks,
                sortType,
                languageCode,
                sortPriorityRules,
                sortIntoBundles
        );
        if (sortedInventoryLayout.stacks().stream().allMatch(ItemStack::isEmpty)) {
            return;
        }

        ContainerStacks.set(container, startSlot, sortedInventoryLayout.stacks());
        container.setChanged();
    }
}
