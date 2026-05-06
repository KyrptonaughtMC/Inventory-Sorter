package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.minecraft.world.inventory.ContainerInput;

public record PlannedContainerClick(int slotIndex, int button, ContainerInput input) {
}
