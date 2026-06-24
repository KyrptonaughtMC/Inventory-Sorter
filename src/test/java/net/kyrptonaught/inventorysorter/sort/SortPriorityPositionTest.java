package net.kyrptonaught.inventorysorter.sort;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SortPriorityPositionTest {
    @Test
    void configValuesAreLowercaseEnumNames() {
        Assertions.assertEquals("first", SortPriorityPosition.FIRST.configValue());
        Assertions.assertEquals("default", SortPriorityPosition.DEFAULT.configValue());
        Assertions.assertEquals("last", SortPriorityPosition.LAST.configValue());
        Assertions.assertEquals("ignore", SortPriorityPosition.IGNORE.configValue());
    }

    @Test
    void configValuesParseCaseInsensitiveAndTrimmed() {
        Assertions.assertEquals(SortPriorityPosition.FIRST, SortPriorityPosition.fromConfigValue(" FIRST "));
        Assertions.assertEquals(SortPriorityPosition.DEFAULT, SortPriorityPosition.fromConfigValue("default"));
        Assertions.assertEquals(SortPriorityPosition.LAST, SortPriorityPosition.fromConfigValue("Last"));
        Assertions.assertEquals(SortPriorityPosition.IGNORE, SortPriorityPosition.fromConfigValue("ignore"));
    }

    @Test
    void translationKeyUsesConfigValue() {
        Assertions.assertEquals(
                "inventorysorter.config.sortPriorityRules.position.ignore",
                SortPriorityPosition.IGNORE.getTranslationKey()
        );
    }
}
