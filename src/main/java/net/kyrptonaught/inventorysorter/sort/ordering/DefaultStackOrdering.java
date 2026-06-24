package net.kyrptonaught.inventorysorter.sort.ordering;

import net.kyrptonaught.inventorysorter.language.MinecraftLocale;
import net.minecraft.world.item.ItemStack;

import java.text.Collator;
import java.util.Comparator;

final class DefaultStackOrdering {
    private DefaultStackOrdering() {
    }

    static Comparator<ItemStack> comparator(String languageCode) {
        Collator collator = Collator.getInstance(MinecraftLocale.fromLanguageCode(languageCode));
        return Comparator.comparing(StackNameOrderingKey::value, collator)
                .thenComparing(OminousBlockOrderingKey::value)
                .thenComparing(OminousBottleOrderingKey::amplifier)
                .thenComparing(ItemStack::getDamageValue)
                .thenComparing(ItemStack::getCount, Comparator.reverseOrder());
    }
}
