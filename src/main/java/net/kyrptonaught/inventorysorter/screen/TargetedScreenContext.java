package net.kyrptonaught.inventorysorter.screen;

import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record TargetedScreenContext(AbstractContainerMenu menu, InventoryScreenId screenId, Container inventory) {
}
