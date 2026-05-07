package net.kyrptonaught.inventorysorter.inventory;

import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.inventory.container.ScreenInventory;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public final class ServerInventorySorter {
    private ServerInventorySorter() {
    }

    public static boolean sort(ServerPlayer player, SortTarget target, SortType sortType) {
        return sort(player, target, new SortSettings(true, false, true, sortType));
    }

    public static boolean sort(ServerPlayer player, SortTarget target, SortSettings settings) {
        String languageCode = player.clientInformation().language().toLowerCase();
        if (target == SortTarget.PLAYER_INVENTORY) {
            PlayerInventorySorter.sort(player, settings, languageCode);
            return true;
        }

        if (target == SortTarget.CONTAINER && SortabilityPolicy.canSortInventory(player)) {
            Container inventory = ScreenInventory.fromMenu(player.containerMenu);
            if (inventory != null) {
                ContainerInventorySorter.sort(
                        inventory,
                        0,
                        inventory.getContainerSize(),
                        settings.sortType(),
                        languageCode,
                        settings.sortPriorityRules(),
                        settings.sortIntoBundles()
                );
                return true;
            }
        }
        return false;
    }
}
