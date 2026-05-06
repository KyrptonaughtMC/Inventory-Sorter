package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

public record ClientSortScope(int menuId, List<ScopedSlot> slots, List<ScopedSlot> hotbarBundleTargetSlots) {
    private static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    private static final int LAST_MAIN_INVENTORY_SLOT = 35;
    private static final int FIRST_HOTBAR_SLOT = 0;
    private static final int LAST_HOTBAR_SLOT = 8;

    public static Optional<ClientSortScope> resolve(Minecraft minecraft, SortTarget target) {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) {
            return Optional.empty();
        }

        Player player = minecraft.player;
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || !menu.stillValid(player) || player.isSpectator() || !menu.getCarried().isEmpty()) {
            return Optional.empty();
        }

        return resolve(
                menu,
                player.getInventory(),
                target,
                player,
                () -> InventoryHelper.canSortInventory(player, menu)
        );
    }

    static Optional<ClientSortScope> resolve(
            AbstractContainerMenu menu,
            Container playerInventory,
            SortTarget target,
            Player player
    ) {
        return resolve(menu, playerInventory, target, player, () -> true);
    }

    static Optional<ClientSortScope> resolve(
            AbstractContainerMenu menu,
            Container playerInventory,
            SortTarget target,
            Player player,
            BooleanSupplier canSortContainer
    ) {
        if (target == SortTarget.CONTAINER && !canSortContainer.getAsBoolean()) {
            return Optional.empty();
        }

        List<ScopedSlot> slots = target == SortTarget.PLAYER_INVENTORY
                ? playerInventorySlots(menu, playerInventory)
                : containerSlots(menu);

        if (slots.isEmpty() || slots.stream().anyMatch(slot -> !canFallbackSort(slot.slot(), player))) {
            return Optional.empty();
        }

        List<ScopedSlot> hotbarBundleTargetSlots = target == SortTarget.PLAYER_INVENTORY
                ? hotbarSlots(menu, playerInventory, player)
                : List.of();

        return Optional.of(new ClientSortScope(menu.containerId, List.copyOf(slots), List.copyOf(hotbarBundleTargetSlots)));
    }

    private static List<ScopedSlot> playerInventorySlots(AbstractContainerMenu menu, Container playerInventory) {
        return IntStream.range(0, menu.slots.size())
                .mapToObj(index -> new ScopedSlot(index, menu.slots.get(index)))
                .filter(slot -> slot.container() == playerInventory)
                .filter(slot -> slot.getContainerSlot() >= FIRST_MAIN_INVENTORY_SLOT)
                .filter(slot -> slot.getContainerSlot() <= LAST_MAIN_INVENTORY_SLOT)
                .toList();
    }

    private static List<ScopedSlot> hotbarSlots(AbstractContainerMenu menu, Container playerInventory, Player player) {
        return IntStream.range(0, menu.slots.size())
                .mapToObj(index -> new ScopedSlot(index, menu.slots.get(index)))
                .filter(slot -> slot.container() == playerInventory)
                .filter(slot -> slot.getContainerSlot() >= FIRST_HOTBAR_SLOT)
                .filter(slot -> slot.getContainerSlot() <= LAST_HOTBAR_SLOT)
                .filter(slot -> canFallbackSort(slot.slot(), player))
                .toList();
    }

    private static List<ScopedSlot> containerSlots(AbstractContainerMenu menu) {
        Container inventory = InventoryHelper.getInventory(menu);
        if (inventory == null) {
            return List.of();
        }

        return IntStream.range(0, menu.slots.size())
                .mapToObj(index -> new ScopedSlot(index, menu.slots.get(index)))
                .filter(slot -> slot.container() == inventory)
                .toList();
    }

    private static boolean canFallbackSort(Slot slot, Player player) {
        return slot.isActive() && !slot.isFake() && slot.allowModification(player);
    }

    public record ScopedSlot(int menuSlotIndex, Slot slot) {
        private Container container() {
            return slot.container;
        }

        private int getContainerSlot() {
            return slot.getContainerSlot();
        }
    }
}
