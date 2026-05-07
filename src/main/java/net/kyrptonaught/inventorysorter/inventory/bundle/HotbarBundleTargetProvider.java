package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.inventory.container.InventorySlice;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public final class HotbarBundleTargetProvider implements BundleTargetProvider {
    @Override
    public List<Container> containers(ServerPlayer player, SortSettings settings) {
        if (!settings.sortIntoHotbarBundles()) {
            return List.of();
        }
        return List.of(new InventorySlice(player.getInventory(), 0, 9));
    }
}
