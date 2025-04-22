package net.kyrptonaught.inventorysorter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.text.Collator;
import java.util.*;
import java.util.stream.IntStream;

public class SortCases {
    static Comparator<ItemStack> getComparator(SortType sortType, String language) {
        Collator collator = Collator.getInstance(fromMinecraftLocale(language));

        var defaultComparator = Comparator.comparing(SortCases::getSortableName, collator)
                .thenComparing(SortCases::isOminous)
                .thenComparing(SortCases::getOminousAmplifier)
                .thenComparing(ItemStack::getDamage)
                .thenComparing(ItemStack::getCount, Comparator.reverseOrder());
        switch (sortType) {
            case CATEGORY -> {
                return Comparator.comparing(SortCases::getGroupIdentifier).thenComparing(defaultComparator);
            }
            case MOD -> {
                return Comparator.comparing((ItemStack stack) -> Registries.ITEM.getId(stack.getItem()).getNamespace()).thenComparing(defaultComparator);
            }
            case NAME -> {
                return defaultComparator;
            }
            case ID -> {
                // @TODO: check this
                return Comparator.comparing((ItemStack stack) -> Registries.ITEM.getId(stack.getItem()).toString()).thenComparing(defaultComparator);
            }
            default -> {
                return defaultComparator;
            }
        }
    }

    private static int getGroupIdentifier(ItemStack stack) {
        List<ItemGroup> groups = ItemGroups.getGroups();
        for (int i = 0; i < groups.size(); i++) {
            var group = groups.get(i);
            var stacks = group.getSearchTabStacks().stream().toList();
            var index = IntStream
                    .range(0, stacks.size())
                    .filter(j -> ItemStack.areItemsAndComponentsEqual(stacks.get(j), stack))
                    .findFirst();

            if (index.isPresent()) {
                return i * 1000 + index.getAsInt();
            }
        }
        return 99999;
    }

    private static int getOminousAmplifier(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        if (components.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER)) {
            int i = components.get(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER).value() + 1;
            return i;
        }

        return 0;
    }

    private static boolean isOminous(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        if (!components.contains(DataComponentTypes.BLOCK_STATE)) {
            return false;
        }

        String result = components.get(DataComponentTypes.BLOCK_STATE).properties().getOrDefault("ominous", "false");
        return Boolean.parseBoolean(result);
    }

    private static String getSortableName(ItemStack stack) {
        ComponentMap components = stack.getComponents();

        if (components.contains(DataComponentTypes.PROFILE)) {
            return playerHeadName(stack).toLowerCase();
        }

        if (stack.isOf(Items.ENCHANTED_BOOK)) {
            return enchantedBookNameCase(stack).toLowerCase();
        }

        return stackName(stack).toLowerCase();
    }

    private static String playerHeadName(ItemStack stack) {
        ProfileComponent profileComponent = stack.getComponents().get(DataComponentTypes.PROFILE);

        if (profileComponent.name().isPresent()) {
            return profileComponent.name().get();
        }

        return stackName(stack);
    }

    private static String stackName(ItemStack stack) {
        return stack.getName().getString();
    }

    private static String enchantedBookNameCase(ItemStack stack) {
        ItemEnchantmentsComponent enchantmentsComponent = stack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
        List<String> names = new ArrayList<>();
        StringBuilder enchantNames = new StringBuilder();
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchantmentsComponent.getEnchantmentEntries()) {
            names.add(Enchantment.getName(enchant.getKey(), enchant.getIntValue()).getString());
        }
        Collections.sort(names);
        for (String enchant : names) {
            enchantNames.append(enchant).append(" ");
        }
        return stack.getName().getString() + " " + enchantmentsComponent.getSize() + " " + enchantNames;
    }

    private static Locale fromMinecraftLocale(String mcLocale) {
        String[] parts = mcLocale.toLowerCase().split("_");
        if (parts.length == 2) {
            return Locale.of(parts[0], parts[1].toUpperCase());
        } else {
            return Locale.getDefault(); // fallback
        }
    }

}
