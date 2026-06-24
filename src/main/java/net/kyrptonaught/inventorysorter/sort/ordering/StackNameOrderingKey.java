package net.kyrptonaught.inventorysorter.sort.ordering;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class StackNameOrderingKey {
    private StackNameOrderingKey() {
    }

    static String value(ItemStack stack) {
        DataComponentMap components = stack.getComponents();

        if (components.has(DataComponents.PROFILE)) {
            return playerHeadName(stack).toLowerCase();
        }

        if (stack.is(Items.ENCHANTED_BOOK)) {
            return enchantedBookName(stack).toLowerCase();
        }

        return stackName(stack).toLowerCase();
    }

    private static String playerHeadName(ItemStack stack) {
        ResolvableProfile profileComponent = stack.getComponents().get(DataComponents.PROFILE);
        return profileComponent.name().orElseGet(() -> stackName(stack));
    }

    private static String stackName(ItemStack stack) {
        return stack.getHoverName().getString();
    }

    private static String enchantedBookName(ItemStack stack) {
        ItemEnchantments enchantmentsComponent = stack.getComponents().get(DataComponents.STORED_ENCHANTMENTS);
        return formatEnchantedBookName(stack.getHoverName().getString(), enchantmentsComponent.size(), sortedEnchantmentNames(enchantmentsComponent));
    }

    static String formatEnchantedBookName(String hoverName, int enchantmentCount, List<String> enchantmentNames) {
        StringBuilder enchantNames = new StringBuilder();
        for (String enchant : enchantmentNames) {
            enchantNames.append(enchant).append(" ");
        }
        return hoverName + " " + enchantmentCount + " " + enchantNames;
    }

    private static List<String> sortedEnchantmentNames(ItemEnchantments enchantmentsComponent) {
        List<String> names = new ArrayList<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchantmentsComponent.entrySet()) {
            names.add(Enchantment.getFullname(enchant.getKey(), enchant.getIntValue()).getString());
        }
        Collections.sort(names);
        return names;
    }
}
