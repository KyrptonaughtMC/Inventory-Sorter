package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class ClientSortScopeTest {
    @Test
    void playerInventoryScopeUsesMainInventorySlotsAndExcludesHotbar() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(9);
        Container playerInventory = new SimpleContainer(36);
        addSlots(menu, container, 0, 9);
        addSlots(menu, playerInventory, 0, 36);

        Optional<ClientSortScope> scope = ClientSortScope.resolve(menu, playerInventory, SortTarget.PLAYER_INVENTORY, null);

        Assertions.assertTrue(scope.isPresent());
        Assertions.assertEquals(27, scope.get().slots().size());
        Assertions.assertEquals(9, scope.get().slots().getFirst().slot().getContainerSlot());
        Assertions.assertEquals(35, scope.get().slots().getLast().slot().getContainerSlot());
        Assertions.assertEquals(18, scope.get().slots().getFirst().menuSlotIndex());
        Assertions.assertEquals(44, scope.get().slots().getLast().menuSlotIndex());
    }

    @Test
    void playerInventoryScopeIncludesHotbarAndCompatibilitySlotsAsExtraBundleTargets() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(9);
        Container playerInventory = new SimpleContainer(36);
        Container compatibilityInventory = new SimpleContainer(2);
        addSlots(menu, container, 0, 9);
        addSlots(menu, playerInventory, 0, 36);
        addSlots(menu, compatibilityInventory, 0, 2);

        Optional<ClientSortScope> scope = ClientSortScope.resolve(
                menu,
                playerInventory,
                SortTarget.PLAYER_INVENTORY,
                null,
                () -> true,
                candidate -> candidate.container == compatibilityInventory
        );

        Assertions.assertTrue(scope.isPresent());
        Assertions.assertEquals(9, scope.get().hotbarBundleTargetSlots().size());
        Assertions.assertEquals(0, scope.get().hotbarBundleTargetSlots().getFirst().slot().getContainerSlot());
        Assertions.assertEquals(2, scope.get().compatibilityBundleTargetSlots().size());
        Assertions.assertEquals(0, scope.get().compatibilityBundleTargetSlots().getFirst().slot().getContainerSlot());
        Assertions.assertEquals(45, scope.get().compatibilityBundleTargetSlots().get(0).menuSlotIndex());
        Assertions.assertEquals(46, scope.get().compatibilityBundleTargetSlots().get(1).menuSlotIndex());
    }

    @Test
    void playerInventoryScopeIncludesInactiveCompatibilitySlotsBecausePluginsCanPrepareThemBeforeClicking() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(9);
        Container playerInventory = new SimpleContainer(36);
        Container compatibilityInventory = new SimpleContainer(1);
        addSlots(menu, container, 0, 9);
        addSlots(menu, playerInventory, 0, 36);
        menu.add(new InactiveSlot(compatibilityInventory, 0));

        Optional<ClientSortScope> scope = ClientSortScope.resolve(
                menu,
                playerInventory,
                SortTarget.PLAYER_INVENTORY,
                null,
                () -> true,
                candidate -> candidate.container == compatibilityInventory
        );

        Assertions.assertTrue(scope.isPresent());
        Assertions.assertEquals(1, scope.get().compatibilityBundleTargetSlots().size());
        Assertions.assertEquals(45, scope.get().compatibilityBundleTargetSlots().getFirst().menuSlotIndex());
    }

    @Test
    void containerScopeUsesOnlyTheFirstBackingContainer() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(9);
        Container playerInventory = new SimpleContainer(36);
        addSlots(menu, container, 0, 9);
        addSlots(menu, playerInventory, 0, 36);

        Optional<ClientSortScope> scope = ClientSortScope.resolve(menu, playerInventory, SortTarget.CONTAINER, null);

        Assertions.assertTrue(scope.isPresent());
        Assertions.assertEquals(9, scope.get().slots().size());
        Assertions.assertTrue(scope.get().slots().stream().allMatch(scopedSlot -> scopedSlot.slot().container == container));
        Assertions.assertEquals(0, scope.get().slots().getFirst().menuSlotIndex());
        Assertions.assertEquals(8, scope.get().slots().getLast().menuSlotIndex());
    }

    @Test
    void containerScopeRejectsFallbackWhenContainerPolicyRejectsSorting() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(9);
        Container playerInventory = new SimpleContainer(36);
        addSlots(menu, container, 0, 9);
        addSlots(menu, playerInventory, 0, 36);

        Optional<ClientSortScope> scope = ClientSortScope.resolve(
                menu,
                playerInventory,
                SortTarget.CONTAINER,
                null,
                () -> false
        );

        Assertions.assertTrue(scope.isEmpty());
    }

    @Test
    void inactiveSlotsRejectFallbackScope() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(1);
        menu.add(new InactiveSlot(container, 0));

        Optional<ClientSortScope> scope = ClientSortScope.resolve(menu, new SimpleContainer(36), SortTarget.CONTAINER, null);

        Assertions.assertTrue(scope.isEmpty());
    }

    @Test
    void fakeSlotsRejectFallbackScope() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(1);
        menu.add(new FakeSlot(container, 0));

        Optional<ClientSortScope> scope = ClientSortScope.resolve(menu, new SimpleContainer(36), SortTarget.CONTAINER, null);

        Assertions.assertTrue(scope.isEmpty());
    }

    @Test
    void nonModifiableSlotsRejectFallbackScope() {
        TestMenu menu = new TestMenu();
        Container container = new SimpleContainer(1);
        menu.add(new NonModifiableSlot(container, 0));

        Optional<ClientSortScope> scope = ClientSortScope.resolve(menu, new SimpleContainer(36), SortTarget.CONTAINER, null);

        Assertions.assertTrue(scope.isEmpty());
    }

    private static void addSlots(TestMenu menu, Container container, int startSlot, int count) {
        for (int i = 0; i < count; i++) {
            menu.add(new Slot(container, startSlot + i, 0, 0));
        }
    }

    private static class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super((MenuType<?>) null, 1);
        }

        private void add(Slot slot) {
            addSlot(slot);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static class InactiveSlot extends Slot {
        private InactiveSlot(Container container, int slot) {
            super(container, slot, 0, 0);
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    private static class FakeSlot extends Slot {
        private FakeSlot(Container container, int slot) {
            super(container, slot, 0, 0);
        }

        @Override
        public boolean isFake() {
            return true;
        }
    }

    private static class NonModifiableSlot extends Slot {
        private NonModifiableSlot(Container container, int slot) {
            super(container, slot, 0, 0);
        }

        @Override
        public boolean allowModification(Player player) {
            return false;
        }
    }
}
