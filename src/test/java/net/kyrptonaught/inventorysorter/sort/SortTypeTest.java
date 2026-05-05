package net.kyrptonaught.inventorysorter.sort;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SortTypeTest {
    @Test
    void translationKeysUseLowercaseSortTypeNames() {
        Assertions.assertEquals("inventorysorter.sorttype.name", SortType.NAME.getTranslationKey());
        Assertions.assertEquals("inventorysorter.sorttype.category", SortType.CATEGORY.getTranslationKey());
        Assertions.assertEquals("inventorysorter.sorttype.mod", SortType.MOD.getTranslationKey());
        Assertions.assertEquals("inventorysorter.sorttype.id", SortType.ID.getTranslationKey());
    }
}
