package net.kyrptonaught.inventorysorter.inventory;

import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public final class SortabilityPolicy {
    private SortabilityPolicy() {
    }

    public static boolean canSortInventory(Player player) {
        if (player.containerMenu instanceof InventoryMenu) {
            return false;
        }
        return canSortInventory(player, player.containerMenu);
    }

    public static boolean canSortInventory(Player player, AbstractContainerMenu menu) {
        if (menu == null || !menu.stillValid(player)) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }

        try {
            InventoryScreenId screenId = InventoryScreenId.fromMenu(menu).orElse(null);

            if (screenId == null) {
                return false;
            }
            return isSortableContainer(player, menu, screenId);

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static boolean isSortableContainer(Player player, AbstractContainerMenu menu, InventoryScreenId screenId) {
        PlayerSortPrevention playerSortPrevention = player instanceof ServerPlayer serverPlayer
                ? PlatformServices.PLAYER_DATA.getPlayerSortPrevention(serverPlayer)
                : PlayerSortPrevention.DEFAULT;
        if (!compatibility.isSortAllowed(screenId.value(), playerSortPrevention.preventSortForScreens())) {
            return false;
        }

        int numSlots = menu.slots.size();
        if (numSlots <= 36) {
            return false;
        }
        return numSlots - 36 >= 9;
    }
}
