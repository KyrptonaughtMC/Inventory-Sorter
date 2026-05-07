package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public interface BundleTargetProvider {
    List<Container> containers(ServerPlayer player, SortSettings settings);
}
