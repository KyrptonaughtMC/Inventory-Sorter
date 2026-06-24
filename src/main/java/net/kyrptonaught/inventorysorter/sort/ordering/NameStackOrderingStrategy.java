package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

record NameStackOrderingStrategy(String languageCode) implements StackOrderingStrategy {
    @Override
    public Comparator<ItemStack> comparator() {
        return DefaultStackOrdering.comparator(languageCode);
    }
}
