package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

final class OminousBottleOrderingKey {
    private OminousBottleOrderingKey() {
    }

    static int amplifier(ItemStack stack) {
        DataComponentMap components = stack.getComponents();
        if (components.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER)) {
            return components.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER).value() + 1;
        }

        return 0;
    }
}
