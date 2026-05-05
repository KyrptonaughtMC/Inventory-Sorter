package net.kyrptonaught.inventorysorter.sort.ordering;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class CreativeTabOrderingKey {
    private CreativeTabOrderingKey() {
    }

    static int position(ItemStack stack) {
        List<CreativeModeTab> groups = CreativeModeTabs.allTabs();
        List<List<ItemStack>> groupStacks = new ArrayList<>();
        for (CreativeModeTab group : groups) {
            groupStacks.add(group.getSearchTabDisplayItems().stream().toList());
        }
        return position(stack, groupStacks);
    }

    static int position(ItemStack stack, List<List<ItemStack>> groups) {
        for (int i = 0; i < groups.size(); i++) {
            List<ItemStack> stacks = groups.get(i);
            for (int j = 0; j < stacks.size(); j++) {
                if (ItemStack.isSameItemSameComponents(stacks.get(j), stack)) {
                    return i * 1000 + j;
                }
            }
        }
        return 99999;
    }
}
