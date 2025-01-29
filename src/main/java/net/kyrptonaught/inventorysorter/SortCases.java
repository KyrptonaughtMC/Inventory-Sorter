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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class SortCases {
    static Comparator<ItemStack> getComparator(SortType sortType) {
        var defaultComparator = Comparator.comparing(SortCases::getSortableName);
        switch (sortType) {
            case CATEGORY -> {
                return Comparator.comparing(SortCases::getGroupIdentifier).thenComparing(defaultComparator);
            }
            case MOD -> {
                return Comparator.comparing((ItemStack stack) -> Registries.ITEM.getId(stack.getItem()).getNamespace()).thenComparing(defaultComparator);
            }
            case NAME -> {
                return Comparator.comparing(SortCases::getSortableName);
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

    private static String getSortableName(ItemStack stack) {
        ComponentMap component = stack.getComponents();

        if (component != null && component.contains(DataComponentTypes.PROFILE))
            return playerHeadCase(stack);
        if (stack.isOf(Items.ENCHANTED_BOOK))
            return enchantedBookNameCase(stack);
        if (stack.isDamageable())
            return toolDuribilityCase(stack);
        return stackSize(stack);
    }

    private static String playerHeadCase(ItemStack stack) {
        ProfileComponent profileComponent = stack.getComponents().get(DataComponentTypes.PROFILE);
        String ownerName = profileComponent.name().isPresent() ? profileComponent.name().get() : stack.getName().getString();

        // this is duplicated logic, so we should probably refactor
        String count = "";
        if (stack.getCount() != stack.getMaxCount()) {
            count = Integer.toString(stack.getCount());
        }

        return ownerName + count;
    }

    private static String stackSize(ItemStack stack) {
        String postfix = (stack.getCount() == stack.getMaxCount()) ? "0" : String.valueOf(stack.getCount());
        //We're returning a string to be used as the basis of a comparison.
        // Full stacks need to come before non-full stacks, hence the 0 postfix.
        return stack.getName().getString() + postfix;
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
