package net.kyrptonaught.inventorysorter.compat;

import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class TrinketsBundleTargets {
    private static final String TRINKETS_MOD_ID = "trinkets";

    private TrinketsBundleTargets() {
    }

    /**
     * Returns the player's Trinkets inventories when Trinkets is present.
     *
     * <p>The direct Trinkets API call is isolated behind the loaded-mod check so this mod can run
     * without Trinkets on the runtime classpath.
     */
    public static List<Container> containers(Player player) {
        if (!isLoaded()) {
            return List.of();
        }
        return TrinketsApiBundleTargets.containers(player);
    }

    /**
     * Detects Trinkets-backed menu slots without loading Trinkets classes unless the mod is present.
     */
    public static boolean isTrinketInventory(Container container) {
        if (!isLoaded()) {
            return false;
        }
        return TrinketsApiBundleTargets.isTrinketInventory(container);
    }

    private static boolean isLoaded() {
        return PlatformServices.PLATFORM.isModLoaded(TRINKETS_MOD_ID);
    }
}
