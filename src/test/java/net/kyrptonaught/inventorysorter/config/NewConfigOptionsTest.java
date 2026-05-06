package net.kyrptonaught.inventorysorter.config;

import net.kyrptonaught.inventorysorter.sort.SortType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NewConfigOptionsTest {
    @Test
    void oldConfigMigrationCopiesServerSafeOptions() {
        OldConfigOptions oldOptions = new OldConfigOptions();
        oldOptions.displaySort = false;
        oldOptions.displayTooltip = false;
        oldOptions.seperateBtn = false;
        oldOptions.sortPlayer = true;
        oldOptions.sortType = SortType.ID;
        oldOptions.doubleClickSort = false;
        oldOptions.sortMouseHighlighted = false;
        oldOptions.keybinding = "key.keyboard.g";

        NewConfigOptions newOptions = NewConfigOptions.convertOldToNew(oldOptions);

        Assertions.assertFalse(newOptions.showSortButton);
        Assertions.assertFalse(newOptions.showTooltips);
        Assertions.assertFalse(newOptions.separateButton);
        Assertions.assertTrue(newOptions.sortPlayerInventory);
        Assertions.assertEquals(SortType.ID, newOptions.sortType);
        Assertions.assertFalse(newOptions.enableDoubleClickSort);
        Assertions.assertFalse(newOptions.sortHighlightedItem);
    }
}
