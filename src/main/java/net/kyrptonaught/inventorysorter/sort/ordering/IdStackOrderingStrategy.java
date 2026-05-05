package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

record IdStackOrderingStrategy(String languageCode) implements StackOrderingStrategy {
    @Override
    public Comparator<ItemStack> comparator() {
        return Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .thenComparing(DefaultStackOrdering.comparator(languageCode));
    }
}
