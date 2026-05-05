package net.kyrptonaught.inventorysorter.sort;

import net.kyrptonaught.inventorysorter.InventorySorterMod;

public enum SortType {
    NAME, CATEGORY, MOD, ID;

    public String getTranslationKey() {
        return InventorySorterMod.MOD_ID + ".sorttype." + this.name().toLowerCase();
    }
}
