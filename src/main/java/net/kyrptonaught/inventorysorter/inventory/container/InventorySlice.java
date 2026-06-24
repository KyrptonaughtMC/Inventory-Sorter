package net.kyrptonaught.inventorysorter.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record InventorySlice(Container container, int startSlot, int size) implements Container {
    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return container.getItem(startSlot + slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return container.removeItem(startSlot + slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return container.removeItemNoUpdate(startSlot + slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        container.setItem(startSlot + slot, stack);
    }

    @Override
    public void setChanged() {
        container.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < size; i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }
}
