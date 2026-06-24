package net.kyrptonaught.inventorysorter.client.sort.plan;

import net.minecraft.world.item.ItemStack;

public record SlotState(int menuSlotIndex, ItemStack stack) {
}
