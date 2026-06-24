package net.kyrptonaught.inventorysorter;

import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class InventoryScreenIdTest {
    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void registeredMenuUsesMenuTypeRegistryId() {
        Optional<InventoryScreenId> screenId = InventoryScreenId.fromMenu(new TestMenu(MenuType.GENERIC_9x3));

        Assertions.assertTrue(screenId.isPresent());
        Assertions.assertEquals(Identifier.parse("minecraft:generic_9x3"), screenId.get().value());
        Assertions.assertEquals("minecraft:generic_9x3", screenId.get().serialized());
    }

    @Test
    void missingMenuHasNoScreenId() {
        Assertions.assertTrue(InventoryScreenId.fromMenu(null).isEmpty());
    }

    @Test
    void unregisteredMenuTypeHasNoScreenId() {
        Optional<InventoryScreenId> screenId = InventoryScreenId.fromMenu(new TestMenu(null));

        Assertions.assertTrue(screenId.isEmpty());
    }

    @Test
    void playerInventoryKeepsHistoricalSyntheticId() {
        Assertions.assertEquals(Identifier.parse("player_inventory"), InventoryScreenId.PLAYER_INVENTORY.value());
        Assertions.assertEquals(Identifier.parse("player_inventory").toString(), InventoryScreenId.PLAYER_INVENTORY.serialized());
    }

    private static class TestMenu extends AbstractContainerMenu {
        private TestMenu(MenuType<?> menuType) {
            super(menuType, 1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
