package net.kyrptonaught.inventorysorter;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static net.minecraft.core.component.DataComponents.ITEM_NAME;

public class SortableItemStackRulesTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void sameIdentityUsesMinecraftItemAndComponentEquality() {
        Assertions.assertTrue(SortableItemStackRules.sameIdentity(stack(Items.DIAMOND, 1), stack(Items.DIAMOND, 64)));
        Assertions.assertFalse(SortableItemStackRules.sameIdentity(stack(Items.DIAMOND, 1), stack(Items.APPLE, 1)));
        Assertions.assertFalse(SortableItemStackRules.sameIdentity(ItemStack.EMPTY, ItemStack.EMPTY));
    }

    @Test
    void differentComponentsAreDifferentIdentities() {
        ItemStack namedDiamond = stack(Items.DIAMOND, 1);
        namedDiamond.set(ITEM_NAME, Component.literal("Named Diamond"));

        Assertions.assertFalse(SortableItemStackRules.sameIdentity(stack(Items.DIAMOND, 1), namedDiamond));
    }

    @Test
    void sameLayoutStackIncludesCountAndEmptySlots() {
        Assertions.assertTrue(SortableItemStackRules.sameLayoutStack(ItemStack.EMPTY, ItemStack.EMPTY));
        Assertions.assertTrue(SortableItemStackRules.sameLayoutStack(stack(Items.DIAMOND, 32), stack(Items.DIAMOND, 32)));
        Assertions.assertFalse(SortableItemStackRules.sameLayoutStack(stack(Items.DIAMOND, 32), stack(Items.DIAMOND, 33)));
    }

    @Test
    void mergeRulesRespectIdentityStackabilityAndCapacity() {
        Assertions.assertTrue(SortableItemStackRules.canMerge(stack(Items.DIAMOND, 10), stack(Items.DIAMOND, 20)));
        Assertions.assertFalse(SortableItemStackRules.canMerge(stack(Items.DIAMOND, 10), stack(Items.APPLE, 20)));
        Assertions.assertFalse(SortableItemStackRules.canMerge(stack(Items.DIAMOND, 10), stack(Items.DIAMOND, 64)));
    }

    @Test
    void mergeIntoMovesOnlyAvailableCapacity() {
        ItemStack source = stack(Items.DIAMOND, 33);
        ItemStack target = stack(Items.DIAMOND, 32);

        int moved = SortableItemStackRules.mergeInto(source, target);

        Assertions.assertEquals(32, moved);
        Assertions.assertEquals(1, source.getCount());
        Assertions.assertEquals(64, target.getCount());
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item),
                count,
                DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 64).build()
        );
    }
}
