package net.kyrptonaught.inventorysorter.sort;

import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SortPriorityRuleChainTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void emptyChainDoesNotIgnoreStacksOrReturnPriorityDecision() {
        SortPriorityRuleChain chain = SortPriorityRuleChain.compile(List.of());

        Assertions.assertTrue(chain.isEmpty());
        Assertions.assertFalse(chain.shouldIgnore(stack(Items.APPLE)));
        Assertions.assertTrue(chain.firstPriorityDecision(stack(Items.APPLE)).isEmpty());
    }

    @Test
    void ignoresStackWhenAnyIgnoreRuleMatchesRegardlessOfRuleOrder() {
        SortPriorityRuleChain chain = SortPriorityRuleChain.compile(List.of(
                new SortPriorityRule("minecraft:bundle", SortPriorityPosition.FIRST),
                new SortPriorityRule("@minecraft:bundle_contents", SortPriorityPosition.IGNORE)
        ));

        Assertions.assertTrue(chain.shouldIgnore(bundle()));
    }

    @Test
    void firstMatchingNonIgnoreRuleWinsPriorityDecision() {
        SortPriorityRuleChain chain = SortPriorityRuleChain.compile(List.of(
                new SortPriorityRule("minecraft:bundle", SortPriorityPosition.LAST),
                new SortPriorityRule("@minecraft:bundle_contents", SortPriorityPosition.FIRST)
        ));

        SortPriorityDecision decision = chain.firstPriorityDecision(bundle()).orElseThrow();

        Assertions.assertEquals(SortPriorityPosition.LAST, decision.position());
        Assertions.assertEquals(0, decision.ruleOrder());
    }

    @Test
    void ignoreRulesAreSkippedWhenAskingForPriorityDecision() {
        SortPriorityRuleChain chain = SortPriorityRuleChain.compile(List.of(
                new SortPriorityRule("@minecraft:bundle_contents", SortPriorityPosition.IGNORE),
                new SortPriorityRule("minecraft:bundle", SortPriorityPosition.FIRST)
        ));

        SortPriorityDecision decision = chain.firstPriorityDecision(bundle()).orElseThrow();

        Assertions.assertEquals(SortPriorityPosition.FIRST, decision.position());
        Assertions.assertEquals(1, decision.ruleOrder());
    }

    @Test
    void invalidRulesDoNotEnterTheChain() {
        SortPriorityRuleChain chain = SortPriorityRuleChain.compile(List.of(
                new SortPriorityRule("@", SortPriorityPosition.FIRST)
        ));

        Assertions.assertTrue(chain.isEmpty());
    }

    @Test
    void defaultPriorityDecisionUsesDefaultOrderingBucket() {
        SortPriorityDecision decision = new SortPriorityDecision(SortPriorityPosition.DEFAULT, 4);

        SortPriorityDecision.PriorityKey priorityKey = decision.priorityKey();

        Assertions.assertEquals(1, priorityKey.bucket());
        Assertions.assertEquals(4, priorityKey.ruleOrder());
    }

    @Test
    void ignoreDecisionIsNotAnOrderingDecision() {
        SortPriorityDecision decision = new SortPriorityDecision(SortPriorityPosition.IGNORE, 0);

        Assertions.assertTrue(decision.ignoresStack());
        Assertions.assertThrows(IllegalStateException.class, decision::priorityKey);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(
                Holder.direct(item),
                1,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }

    private static ItemStack bundle() {
        return new ItemStack(
                Holder.direct(Items.BUNDLE),
                1,
                DataComponentPatch.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 1)
                        .set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                        .build()
        );
    }
}
