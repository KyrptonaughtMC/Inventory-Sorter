package net.kyrptonaught.inventorysorter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.text.Collator;
import java.util.*;
import java.util.stream.IntStream;

public class SortCases {
    static Comparator<ItemStack> getComparator(SortType sortType, String language) {
        Collator collator = Collator.getInstance(fromMinecraftLocale(language));

        var defaultComparator = Comparator.comparing(SortCases::getSortableName, collator)
                .thenComparing(SortCases::isOminous)
                .thenComparing(SortCases::getOminousAmplifier)
                .thenComparing(ItemStack::getDamageValue)
                .thenComparing(ItemStack::getCount, Comparator.reverseOrder());
        switch (sortType) {
            case CATEGORY -> {
                return Comparator.comparing(SortCases::getGroupIdentifier).thenComparing(defaultComparator);
            }
            case MOD -> {
                return Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()).thenComparing(defaultComparator);
            }
            case NAME -> {
                return defaultComparator;
            }
            case ID -> {
                // @TODO: check this
                return Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).thenComparing(defaultComparator);
            }
            default -> {
                return defaultComparator;
            }
        }
    }

    private static int getGroupIdentifier(ItemStack stack) {
        List<CreativeModeTab> groups = CreativeModeTabs.allTabs();
        for (int i = 0; i < groups.size(); i++) {
            var group = groups.get(i);
            var stacks = group.getSearchTabDisplayItems().stream().toList();
            var index = IntStream
                    .range(0, stacks.size())
                    .filter(j -> ItemStack.isSameItemSameComponents(stacks.get(j), stack))
                    .findFirst();

            if (index.isPresent()) {
                return i * 1000 + index.getAsInt();
            }
        }
        return 99999;
    }

    private static int getOminousAmplifier(ItemStack stack) {
        DataComponentMap components = stack.getComponents();
        if (components.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER)) {
            int i = components.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER).value() + 1;
            return i;
        }

        return 0;
    }

    private static boolean isOminous(ItemStack stack) {
        DataComponentMap components = stack.getComponents();
        if (!components.has(DataComponents.BLOCK_STATE)) {
            return false;
        }

        String result = components.get(DataComponents.BLOCK_STATE).properties().getOrDefault("ominous", "false");
        return Boolean.parseBoolean(result);
    }

    private static String getSortableName(ItemStack stack) {
        DataComponentMap components = stack.getComponents();

        if (components.has(DataComponents.PROFILE)) {
            return playerHeadName(stack).toLowerCase();
        }

        if (stack.is(Items.ENCHANTED_BOOK)) {
            return enchantedBookNameCase(stack).toLowerCase();
        }

        return stackName(stack).toLowerCase();
    }

    private static String playerHeadName(ItemStack stack) {
        ResolvableProfile profileComponent = stack.getComponents().get(DataComponents.PROFILE);
        Optional<String> componentName = profileComponent.name();

        return componentName.orElseGet(() -> stackName(stack));

    }

    private static String stackName(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    private static String enchantedBookNameCase(ItemStack stack) {
        ItemEnchantments enchantmentsComponent = stack.getComponents().get(DataComponents.STORED_ENCHANTMENTS);
        List<String> names = new ArrayList<>();
        StringBuilder enchantNames = new StringBuilder();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchantmentsComponent.entrySet()) {
            names.add(Enchantment.getFullname(enchant.getKey(), enchant.getIntValue()).getString());
        }
        Collections.sort(names);
        for (String enchant : names) {
            enchantNames.append(enchant).append(" ");
        }
        return stack.getHoverName().getString() + " " + enchantmentsComponent.size() + " " + enchantNames;
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
