package net.kyrptonaught.inventorysorter.compat;

import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.inventorysorter.compat.plugins.TrinketsPlugin;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

public final class CompatibilityPlugins {
    private static final List<CompatibilityPlugin> PLUGINS = List.of(new TrinketsPlugin());

    private CompatibilityPlugins() {
    }

    public static List<BundleTargetSlot> serverBundleSlots(ServerPlayer player, SortSettings settings) {
        List<BundleTargetSlot> slots = new ArrayList<>();
        for (CompatibilityPlugin plugin : PLUGINS) {
            slots.addAll(plugin.serverBundleSlots(player, settings));
        }
        return List.copyOf(slots);
    }

    public static boolean isClientBundleSlot(Slot slot) {
        for (CompatibilityPlugin plugin : PLUGINS) {
            if (plugin.isClientBundleSlot(slot)) {
                return true;
            }
        }
        return false;
    }

    public static void prepareClientBundleSlotClick(Slot slot) {
        for (CompatibilityPlugin plugin : PLUGINS) {
            plugin.prepareClientBundleSlotClick(slot);
        }
    }
}
