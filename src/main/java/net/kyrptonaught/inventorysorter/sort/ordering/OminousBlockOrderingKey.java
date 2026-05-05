package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

final class OminousBlockOrderingKey {
    private OminousBlockOrderingKey() {
    }

    static boolean value(ItemStack stack) {
        DataComponentMap components = stack.getComponents();
        if (!components.has(DataComponents.BLOCK_STATE)) {
            return false;
        }

        String result = components.get(DataComponents.BLOCK_STATE).properties().getOrDefault("ominous", "false");
        return Boolean.parseBoolean(result);
    }
}
