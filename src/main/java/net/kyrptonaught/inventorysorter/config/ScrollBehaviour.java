package net.kyrptonaught.inventorysorter.config;

import net.kyrptonaught.inventorysorter.InventorySorterMod;

public enum ScrollBehaviour {
    FREE, MODIFIER, DISABLED;

    public String getTranslationKey() {
        return InventorySorterMod.MOD_ID + ".config.scrollbehaviour." + this.name().toLowerCase();
    }
}
