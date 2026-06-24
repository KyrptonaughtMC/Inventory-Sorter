package net.kyrptonaught.inventorysorter.sort.ordering;

import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

@FunctionalInterface
public interface StackOrderingStrategy {
    Comparator<ItemStack> comparator();

    static StackOrderingStrategy bySortType(SortType sortType, String languageCode) {
        return switch (sortType) {
            case NAME -> new NameStackOrderingStrategy(languageCode);
            case CATEGORY -> new CategoryStackOrderingStrategy(languageCode);
            case MOD -> new ModStackOrderingStrategy(languageCode);
            case ID -> new IdStackOrderingStrategy(languageCode);
        };
    }
}
