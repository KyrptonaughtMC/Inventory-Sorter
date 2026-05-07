package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.List;
import net.kyrptonaught.inventorysorter.compat.TrinketsBundleTargets;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public final class TrinketsBundleTargetProvider implements BundleTargetProvider {
    @Override
    public List<Container> containers(ServerPlayer player, SortSettings settings) {
        return TrinketsBundleTargets.containers(player);
    }
}
