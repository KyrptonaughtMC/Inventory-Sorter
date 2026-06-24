package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SortPriorityRulesCommandTest {
    @Test
    void addRuleAppendsToExistingRules() {
        List<SortPriorityRuleSetting> rules = List.of(new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST));
        SortPriorityRuleSetting shulkers = new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST);

        Assertions.assertEquals(
                List.of(rules.getFirst(), shulkers),
                SortPriorityRulesCommand.addRule(rules, shulkers)
        );
    }

    @Test
    void setRuleReplacesOneBasedIndex() {
        SortPriorityRuleSetting bundles = new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting shulkers = new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting containers = new SortPriorityRuleSetting("@minecraft:container", SortPriorityPosition.LAST);

        Assertions.assertEquals(
                List.of(bundles, containers),
                SortPriorityRulesCommand.setRule(List.of(bundles, shulkers), 2, containers)
        );
    }

    @Test
    void setRulePositionKeepsExistingMatch() {
        SortPriorityRuleSetting bundles = new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting shulkers = new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST);

        Assertions.assertEquals(
                List.of(bundles, new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.LAST)),
                SortPriorityRulesCommand.setRulePosition(List.of(bundles, shulkers), 2, SortPriorityPosition.LAST)
        );
    }

    @Test
    void removeRuleRemovesOneBasedIndex() {
        SortPriorityRuleSetting bundles = new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting shulkers = new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST);

        Assertions.assertEquals(
                List.of(shulkers),
                SortPriorityRulesCommand.removeRule(List.of(bundles, shulkers), 1)
        );
    }

    @Test
    void moveRuleMovesWithinListWithoutChangingRule() {
        SortPriorityRuleSetting bundles = new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting shulkers = new SortPriorityRuleSetting("#minecraft:shulker_boxes", SortPriorityPosition.FIRST);
        SortPriorityRuleSetting containers = new SortPriorityRuleSetting("@minecraft:container", SortPriorityPosition.LAST);

        Assertions.assertEquals(
                List.of(shulkers, bundles, containers),
                SortPriorityRulesCommand.moveRule(List.of(bundles, shulkers, containers), 2, SortPriorityRulesCommand.MoveDirection.UP)
        );
        Assertions.assertEquals(
                List.of(bundles, containers, shulkers),
                SortPriorityRulesCommand.moveRule(List.of(bundles, shulkers, containers), 2, SortPriorityRulesCommand.MoveDirection.DOWN)
        );
    }

    @Test
    void invalidIndexesAreRejected() {
        List<SortPriorityRuleSetting> rules = List.of(new SortPriorityRuleSetting("#minecraft:bundles", SortPriorityPosition.FIRST));

        Assertions.assertThrows(IllegalArgumentException.class, () -> SortPriorityRulesCommand.removeRule(rules, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> SortPriorityRulesCommand.removeRule(rules, 2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> SortPriorityRulesCommand.moveRule(rules, 1, SortPriorityRulesCommand.MoveDirection.UP));
        Assertions.assertThrows(IllegalArgumentException.class, () -> SortPriorityRulesCommand.moveRule(rules, 1, SortPriorityRulesCommand.MoveDirection.DOWN));
    }
}
