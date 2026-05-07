package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.inventory.container.InventorySlice;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public final class HotbarBundleTargetProvider implements BundleTargetProvider {
    @Override
    public List<BundleTargetSlot> slots(ServerPlayer player, SortSettings settings) {
        if (!settings.sortIntoHotbarBundles()) {
            return List.of();
        }
        Container hotbar = new InventorySlice(player.getInventory(), 0, 9);
        return java.util.stream.IntStream.range(0, hotbar.getContainerSize())
                .mapToObj(slot -> BundleTargetSlot.fromContainer(hotbar, slot))
                .toList();
    }
}
