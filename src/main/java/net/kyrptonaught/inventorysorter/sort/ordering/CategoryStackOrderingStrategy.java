package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

record CategoryStackOrderingStrategy(String languageCode) implements StackOrderingStrategy {
    @Override
    public Comparator<ItemStack> comparator() {
        return Comparator.comparing((ItemStack stack) -> CreativeTabOrderingKey.position(stack))
                .thenComparing(DefaultStackOrdering.comparator(languageCode));
    }
}
