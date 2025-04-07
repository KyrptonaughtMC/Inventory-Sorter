package net.kyrptonaught.inventorysorter;

public enum SortType {
    NAME, CATEGORY, MOD, ID;

    public String getTranslationKey() {
        return InventorySorterMod.MOD_ID + ".sorttype." + this.toString().toLowerCase();
    }
}
