package net.kyrptonaught.inventorysorter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SortCases {
    static Comparator<ItemStack> getComparator(SortType sortType) {
        var defaultComparator = Comparator.comparing(SortCases::specialCases);
        switch (sortType) {
            case CATEGORY -> {
                return Comparator.comparing(SortCases::getGroupIdentifier).thenComparing(defaultComparator);
            }
            case MOD -> {
                return Comparator.comparing((ItemStack stack) -> {
                    return Registries.ITEM.getId(stack.getItem()).getNamespace();
                }).thenComparing(defaultComparator);
            }
            case NAME -> {
                return Comparator.comparing(stack -> {
                    var name = specialCases(stack);
                    if (stack.hasCustomName()) return stack.getName() + name;
                    return name;
                });
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
                    .filter(j -> ItemStack.canCombine(stacks.get(j), stack))
                    .findFirst();

            if (index.isPresent()) {
                return i * 1000 + index.getAsInt();
            }
        }
        return 99999;
    }

    private static String specialCases(ItemStack stack) {
        Item item = stack.getItem();
        ComponentMap component = stack.getComponents();

        if (component != null && component.contains(DataComponentTypes.PROFILE))
            return playerHeadCase(stack);
        if (stack.getCount() != stack.getMaxCount())
            return stackSize(stack);
        if (stack.isOf(Items.ENCHANTED_BOOK))
            return enchantedBookNameCase(stack);
        if (stack.isDamageable())
            return toolDuribilityCase(stack);
        return item.toString();
    }

    private static String playerHeadCase(ItemStack stack) {
        ProfileComponent profileComponent = stack.getComponents().get(DataComponentTypes.PROFILE);
        String ownerName = profileComponent.name().isPresent() ? profileComponent.name().get() : stack.getItem().toString();

        // this is duplicated logic, so we should probably refactor
        String count = "";
        if (stack.getCount() != stack.getMaxCount()) {
            count = Integer.toString(stack.getCount());
        }

        return stack.getItem().toString() + " " + ownerName + count;
    }

    private static String stackSize(ItemStack stack) {
        return stack.getItem().toString() + stack.getCount();
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
        return stack.getItem().toString() + " " + enchantmentsComponent.getSize() + " " + enchantNames;
    }

    private static String toolDuribilityCase(ItemStack stack) {
        return stack.getItem().toString() + stack.getDamage();
    }

    public enum SortType {
        NAME, CATEGORY, MOD, ID;

        public String getTranslationKey() {
            return "key." + InventorySorterMod.MOD_ID + ".sorttype." + this.toString().toLowerCase();
        }
    }
}
