package net.kyrptonaught.inventorysorter.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public final class CompatibilityPlugins {
    private static final List<PluginRegistration> REGISTRATIONS = List.of(
            new PluginRegistration("trinkets", "net.kyrptonaught.inventorysorter.compat.plugins.TrinketsPlugin")
    );
    private static List<CompatibilityPlugin> plugins;

    private CompatibilityPlugins() {
    }

    public static List<BundleTargetSlot> serverBundleSlots(ServerPlayer player, SortSettings settings) {
        List<BundleTargetSlot> slots = new ArrayList<>();
        for (CompatibilityPlugin plugin : plugins()) {
            slots.addAll(plugin.serverBundleSlots(player, settings));
        }
        return List.copyOf(slots);
    }

    public static boolean isClientBundleSlot(Slot slot) {
        for (CompatibilityPlugin plugin : plugins()) {
            if (plugin.isClientBundleSlot(slot)) {
                return true;
            }
        }
        return false;
    }

    public static void prepareClientBundleSlotClick(Slot slot) {
        for (CompatibilityPlugin plugin : plugins()) {
            plugin.prepareClientBundleSlotClick(slot);
        }
    }

    private static List<CompatibilityPlugin> plugins() {
        if (plugins == null) {
            plugins = loadPlugins();
        }
        return plugins;
    }

    private static List<CompatibilityPlugin> loadPlugins() {
        List<CompatibilityPlugin> loadedPlugins = new ArrayList<>();
        for (PluginRegistration registration : REGISTRATIONS) {
            if (!PlatformServices.PLATFORM.isModLoaded(registration.modId())) {
                continue;
            }
            loadPlugin(registration).ifPresent(loadedPlugins::add);
        }
        return List.copyOf(loadedPlugins);
    }

    private static Optional<CompatibilityPlugin> loadPlugin(PluginRegistration registration) {
        try {
            Class<?> pluginClass = Class.forName(registration.className());
            return Optional.of((CompatibilityPlugin) pluginClass.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            LOGGER.warn("Failed to load compatibility plugin {} for mod {}", registration.className(), registration.modId(), e);
            return Optional.empty();
        }
    }

    private record PluginRegistration(String modId, String className) {
    }
}
