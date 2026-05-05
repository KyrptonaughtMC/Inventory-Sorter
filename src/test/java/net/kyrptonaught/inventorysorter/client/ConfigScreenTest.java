package net.kyrptonaught.inventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kyrptonaught.inventorysorter.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConfigScreenTest {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("inventorysorter", "test"));
    private static final InputConstants.Key CONFIG_KEY = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_P);
    private static final InputConstants.Key SORT_KEY = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_O);

    @Test
    void consumeConfigScreenClickUsesConfigButtonWhenKeysDiffer() {
        RecordingKeyMapping configButton = new RecordingKeyMapping(true);
        RecordingKeyMapping sortButton = new RecordingKeyMapping(true);

        Assertions.assertTrue(ConfigScreen.consumeConfigScreenClick(configButton, sortButton, CONFIG_KEY, SORT_KEY));
        Assertions.assertEquals(1, configButton.consumeClicks);
        Assertions.assertEquals(0, sortButton.consumeClicks);
    }

    @Test
    void consumeConfigScreenClickUsesSortButtonFirstWhenKeysMatch() {
        RecordingKeyMapping configButton = new RecordingKeyMapping(true);
        RecordingKeyMapping sortButton = new RecordingKeyMapping(true);

        Assertions.assertTrue(ConfigScreen.consumeConfigScreenClick(configButton, sortButton, CONFIG_KEY, CONFIG_KEY));
        Assertions.assertEquals(0, configButton.consumeClicks);
        Assertions.assertEquals(1, sortButton.consumeClicks);
    }

    @Test
    void consumeConfigScreenClickFallsBackToConfigButtonWhenMatchingSortButtonHasNoClick() {
        RecordingKeyMapping configButton = new RecordingKeyMapping(true);
        RecordingKeyMapping sortButton = new RecordingKeyMapping(false);

        Assertions.assertTrue(ConfigScreen.consumeConfigScreenClick(configButton, sortButton, CONFIG_KEY, CONFIG_KEY));
        Assertions.assertEquals(1, configButton.consumeClicks);
        Assertions.assertEquals(1, sortButton.consumeClicks);
    }

    @Test
    void consumeConfigScreenClickReturnsFalseWhenNoRelevantButtonWasClicked() {
        RecordingKeyMapping configButton = new RecordingKeyMapping(false);
        RecordingKeyMapping sortButton = new RecordingKeyMapping(false);

        Assertions.assertFalse(ConfigScreen.consumeConfigScreenClick(configButton, sortButton, CONFIG_KEY, CONFIG_KEY));
        Assertions.assertEquals(1, configButton.consumeClicks);
        Assertions.assertEquals(1, sortButton.consumeClicks);
    }

    @Test
    void blankDraftSortPriorityRulesAreNotSaved() {
        Assertions.assertEquals(
                List.of(new SortPriorityRule("minecraft:bundle", SortPriorityPosition.FIRST)),
                ConfigScreen.saveableSortPriorityRules(List.of(
                        new SortPriorityRule("", SortPriorityPosition.DEFAULT),
                        new SortPriorityRule(" minecraft:bundle ", SortPriorityPosition.FIRST),
                        new SortPriorityRule(" ", SortPriorityPosition.LAST)
                ))
        );
    }

    @Test
    void blankSortPriorityRuleMatchesAreValidDrafts() {
        Assertions.assertTrue(ConfigScreen.sortPriorityMatchError("").isEmpty());
        Assertions.assertTrue(ConfigScreen.sortPriorityMatchError("   ").isEmpty());
    }

    @Test
    void nonBlankInvalidSortPriorityRuleMatchesStillShowErrors() {
        Assertions.assertTrue(ConfigScreen.sortPriorityMatchError("@").isPresent());
    }

    private static class RecordingKeyMapping extends KeyMapping {
        private final boolean consumeClickResult;
        private int consumeClicks;

        private RecordingKeyMapping(boolean consumeClickResult) {
            super("inventorysorter.test.key", InputConstants.UNKNOWN.getValue(), CATEGORY);
            this.consumeClickResult = consumeClickResult;
        }

        @Override
        public boolean consumeClick() {
            consumeClicks++;
            return consumeClickResult;
        }
    }
}
