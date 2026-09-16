package net.kyrptonaught.inventorysorter.client;

import java.util.OptionalInt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class PlayerInventoryButtonPosition {
    private static final int HEADER_OFFSET = 12;

    private PlayerInventoryButtonPosition() {
    }

    public static OptionalInt headerY(AbstractContainerMenu menu, Inventory inventory, AbstractContainerMenu wrappedMenu) {
        int storageEnd = inventory.getNonEquipmentItems().size();
        return menu.slots.stream()
                .filter(slot -> slot.container == inventory)
                .filter(slot -> slot.isActive() && !slot.isFake())
                .filter(slot -> {
                    int index = slot.getContainerSlot();
                    if (wrappedMenu != null) {
                        if (index < 0 || index >= wrappedMenu.slots.size()) {
                            return false;
                        }
                        Slot backingSlot = wrappedMenu.getSlot(index);
                        if (backingSlot.container != inventory) {
                            return false;
                        }
                        index = backingSlot.getContainerSlot();
                    }
                    return index >= Inventory.getSelectionSize() && index < storageEnd;
                })
                .mapToInt(slot -> slot.y)
                .min()
                .stream()
                .map(y -> y - HEADER_OFFSET)
                .findFirst();
    }
}
