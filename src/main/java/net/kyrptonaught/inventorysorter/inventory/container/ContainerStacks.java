package net.kyrptonaught.inventorysorter.inventory.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class ContainerStacks {
    private ContainerStacks() {
    }

    public static List<ItemStack> get(Container container, int startSlot, int size) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            stacks.add(container.getItem(startSlot + i));
        }
        return stacks;
    }

    public static void set(Container container, int startSlot, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            container.setItem(startSlot + i, stacks.get(i));
        }
    }
}
