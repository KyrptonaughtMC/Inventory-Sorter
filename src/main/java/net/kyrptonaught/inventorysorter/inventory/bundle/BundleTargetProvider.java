package net.kyrptonaught.inventorysorter.inventory.bundle;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;

public interface BundleTargetProvider {
    List<BundleTargetSlot> slots(ServerPlayer player, SortSettings settings);
}
