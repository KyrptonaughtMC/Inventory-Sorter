package net.kyrptonaught.inventorysorter.client;

import java.util.Optional;
import net.kyrptonaught.inventorysorter.InventoryScreenId;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public final class SortButtonDisplayPolicy {
    private static final long TIMEOUT_MS = 5 * 60 * 1000;
    private static InventoryScreenId lastCheckedId;
    private static long lastCheckedTimestamp;

    private SortButtonDisplayPolicy() {
    }

    public static boolean shouldDisplayButtons(Player player) {
        if (player.containerMenu == null || !player.containerMenu.stillValid(player)) {
            return false;
        }

        if (player.containerMenu instanceof InventoryMenu) {
            return true;
        }

        if (player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu) {
            return true;
        }

        try {
            InventoryScreenId screenId = InventoryScreenId.fromMenu(player.containerMenu).orElse(null);

            if (screenId == null) {
                return false;
            }
            setLastChecked(screenId);
            return compatibility.shouldShowSortButton(screenId.value());

        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public static boolean canSortInventory(Player player) {
        return SortabilityPolicy.canSortInventory(player);
    }

    public static Optional<InventoryScreenId> getLastCheckedId() {
        if (lastCheckedId != null && System.currentTimeMillis() - lastCheckedTimestamp > TIMEOUT_MS) {
            lastCheckedId = null;
        }
        return Optional.ofNullable(lastCheckedId);
    }

    private static void setLastChecked(InventoryScreenId id) {
        lastCheckedId = id;
        lastCheckedTimestamp = System.currentTimeMillis();
    }
}
