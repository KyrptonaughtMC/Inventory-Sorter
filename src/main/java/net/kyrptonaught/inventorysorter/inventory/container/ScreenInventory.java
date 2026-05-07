package net.kyrptonaught.inventorysorter.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ScreenInventory {
    private ScreenInventory() {
    }

    public static Container fromMenu(AbstractContainerMenu menu) {
        if (menu.slots.isEmpty()) return null;
        return menu.slots.getFirst().container;
    }
}
