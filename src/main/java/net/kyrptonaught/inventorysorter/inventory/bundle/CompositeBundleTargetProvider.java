package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public record CompositeBundleTargetProvider(List<BundleTargetProvider> providers) implements BundleTargetProvider {
    @Override
    public List<Container> containers(ServerPlayer player, SortSettings settings) {
        List<Container> containers = new ArrayList<>();
        for (BundleTargetProvider provider : providers) {
            containers.addAll(provider.containers(player, settings));
        }
        return List.copyOf(containers);
    }
}
