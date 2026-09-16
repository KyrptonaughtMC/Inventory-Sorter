package net.kyrptonaught.inventorysorter.client;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlayerInventoryButtonPositionTest {
    @Test
    void vanillaChestButtonUsesStorageHeaderRatherThanOtherInventoriesOrEquipment() {
        Inventory inventory = new Inventory(null, new EntityEquipment());
        TestMenu menu = new TestMenu();
        menu.add(new Slot(new SimpleContainer(27), 9, 152, 18));
        menu.add(new Slot(inventory, 0, 8, 142));
        menu.add(new Slot(inventory, 36, 8, 8));
        menu.add(new Slot(inventory, 35, 152, 121));
        menu.add(new Slot(inventory, 9, 8, 85));

        Assertions.assertEquals(73, PlayerInventoryButtonPosition.headerY(menu, inventory, null).orElseThrow());
    }

    @Test
    void expandedChestButtonUsesTopRowEvenWhenExtraSlotsAreAppendedOutOfOrder() {
        Inventory inventory = expandedInventory();
        TestMenu menu = new TestMenu();
        menu.add(new Slot(inventory, 9, 8, 85));
        menu.add(new Slot(inventory, 35, 152, 121));
        menu.add(new Slot(inventory, 63, 8, 8));
        menu.add(new Slot(inventory, 0, 8, 196));
        menu.add(new Slot(inventory, 62, 152, 175));

        Assertions.assertEquals(73, PlayerInventoryButtonPosition.headerY(menu, inventory, null).orElseThrow());
    }

    @Test
    void expandedStorageCanStartWithAnAdditionalSlotBeforeTheVanillaRows() {
        Inventory inventory = expandedInventory();
        TestMenu menu = new TestMenu();
        menu.add(new Slot(inventory, 9, 8, 139));
        menu.add(new Slot(inventory, 36, 8, 85));

        Assertions.assertEquals(73, PlayerInventoryButtonPosition.headerY(menu, inventory, null).orElseThrow());
    }

    @Test
    void playerOnlyInventoryButtonUsesTheStorageHeader() {
        Inventory inventory = expandedInventory();
        TestMenu menu = new TestMenu();
        menu.add(new Slot(inventory, 63, 8, 8));
        menu.add(new Slot(inventory, 9, 8, 84));
        menu.add(new Slot(inventory, 62, 152, 174));

        Assertions.assertEquals(72, PlayerInventoryButtonPosition.headerY(menu, inventory, null).orElseThrow());
    }

    @Test
    void creativeWrappersUseBackingInventoryIndicesButDisplayedCoordinates() {
        Inventory inventory = expandedInventory();
        TestMenu backingMenu = new TestMenu();
        backingMenu.add(new Slot(new SimpleContainer(1), 0, 0, 0));
        backingMenu.add(new Slot(inventory, 63, 8, 8));
        backingMenu.add(new Slot(inventory, 9, 8, 84));
        backingMenu.add(new Slot(inventory, 62, 152, 174));
        backingMenu.add(new Slot(inventory, 0, 8, 196));
        backingMenu.add(new Slot(inventory, 67, 77, 62));
        TestMenu displayedMenu = new TestMenu();
        displayedMenu.add(new Slot(inventory, 0, 0, 0));
        displayedMenu.add(new Slot(inventory, 1, 54, 6));
        displayedMenu.add(new Slot(inventory, 2, 9, 54));
        displayedMenu.add(new Slot(inventory, 3, 153, 144));
        displayedMenu.add(new Slot(inventory, 4, 9, 166));
        displayedMenu.add(new Slot(inventory, 5, 35, 35));
        displayedMenu.add(new Slot(inventory, -1, 0, 0));
        displayedMenu.add(new Slot(inventory, 99, 0, 0));

        Assertions.assertEquals(42, PlayerInventoryButtonPosition.headerY(displayedMenu, inventory, backingMenu).orElseThrow());
    }

    @Test
    void noStorageSlotsProducesNoButtonPosition() {
        Inventory inventory = new Inventory(null, new EntityEquipment());
        TestMenu menu = new TestMenu();
        Assertions.assertTrue(PlayerInventoryButtonPosition.headerY(menu, inventory, null).isEmpty());
        menu.add(new Slot(inventory, 0, 8, 142));
        menu.add(new Slot(inventory, 40, 77, 62));
        menu.add(new Slot(inventory, -1, 0, 0));
        Assertions.assertTrue(PlayerInventoryButtonPosition.headerY(menu, inventory, null).isEmpty());
    }

    @Test
    void inactiveAndFakeSlotsDoNotAnchorTheButton() {
        Inventory inventory = new Inventory(null, new EntityEquipment());
        TestMenu menu = new TestMenu();
        menu.add(new Slot(inventory, 9, 8, 1) {
            @Override
            public boolean isActive() {
                return false;
            }
        });
        menu.add(new Slot(inventory, 10, 26, 2) {
            @Override
            public boolean isFake() {
                return true;
            }
        });
        menu.add(new Slot(inventory, 11, 44, 84));

        Assertions.assertEquals(72, PlayerInventoryButtonPosition.headerY(menu, inventory, null).orElseThrow());
    }

    private static Inventory expandedInventory() {
        return new Inventory(null, new EntityEquipment()) {
            private final NonNullList<ItemStack> storage = NonNullList.withSize(63, ItemStack.EMPTY);

            @Override
            public NonNullList<ItemStack> getNonEquipmentItems() {
                return storage;
            }
        };
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super((MenuType<?>) null, 1);
        }

        private void add(Slot slot) {
            addSlot(slot);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
