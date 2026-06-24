package net.kyrptonaught.inventorysorter.sort.bundle;

import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;

public record BundleTargetSlot(SlotAccess access, Runnable setChanged) {
    public static BundleTargetSlot fromContainer(Container container, int slot) {
        return new BundleTargetSlot(new SlotAccess() {
            @Override
            public ItemStack get() {
                return container.getItem(slot);
            }

            @Override
            public boolean set(ItemStack stack) {
                container.setItem(slot, stack);
                return true;
            }
        }, container::setChanged);
    }

    ItemStack stack() {
        return access.get();
    }

    void setStack(ItemStack stack) {
        access.set(stack);
    }
}
