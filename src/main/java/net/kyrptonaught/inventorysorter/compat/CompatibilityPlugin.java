package net.kyrptonaught.inventorysorter.compat;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

public interface CompatibilityPlugin {
    default List<BundleTargetSlot> serverBundleSlots(ServerPlayer player, SortSettings settings) {
        return List.of();
    }

    default boolean isClientBundleSlot(Slot slot) {
        return false;
    }

    default void prepareClientBundleSlotClick(Slot slot) {
    }
}
