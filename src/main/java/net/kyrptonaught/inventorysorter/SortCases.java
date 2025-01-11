package net.kyrptonaught.inventorysorter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortCases {
    public enum SortType {
        NAME, CATEGORY, MOD, ID;

        public String getTranslationKey() {
            return "key." + InventorySorterMod.MOD_ID + ".sorttype." + this.toString().toLowerCase();
        }
    }

    static String getStringForSort(ItemStack stack, SortType sortType) {
        Item item = stack.getItem();
        String itemName = specialCases(stack);
        switch (sortType) {
            case CATEGORY -> {
                ItemGroup group = getFirstItemGroup(stack);
                return (group != null ? group.getDisplayName().getString() : "zzz") + itemName;
            }
            case MOD -> {
                return Registries.ITEM.getId(item).getNamespace() + itemName;
            }
            case NAME -> {
                return stack.getName() + itemName;
            }
        }

        return itemName;
    }

    private static ItemGroup getFirstItemGroup(ItemStack stack) {
        List<ItemGroup> groups = ItemGroups.getGroups();
        for (ItemGroup group : groups) {
            if (group.contains(new ItemStack(stack.getItem())))
                return group;

        }
        return null;
    }

    private static String specialCases(ItemStack stack) {
        Item item = stack.getItem();
        ComponentMap component = stack.getComponents();

        if (component != null && component.contains(DataComponentTypes.PROFILE))
            return playerHeadCase(stack);
        if (stack.getCount() != stack.getMaxCount())
            return stackSize(stack);
        if (item instanceof EnchantedBookItem)
            return enchantedBookNameCase(stack);
        if (item instanceof ToolItem)
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
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : enchantmentsComponent.getEnchantmentsMap()) {
            names.add(Registries.ENCHANTMENT.get(enchant.getKey().getKey().get()).getName(enchant.getIntValue()).getString());
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
}
