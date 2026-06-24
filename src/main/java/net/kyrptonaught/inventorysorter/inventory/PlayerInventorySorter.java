package net.kyrptonaught.inventorysorter.inventory;

import java.util.ArrayList;
import java.util.List;
import net.kyrptonaught.inventorysorter.compat.CompatibilityPlugins;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.kyrptonaught.inventorysorter.inventory.bundle.BundleTargetProvider;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlot;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlots;
import net.kyrptonaught.inventorysorter.inventory.bundle.HotbarBundleTargetProvider;
import net.kyrptonaught.inventorysorter.inventory.container.ContainerStacks;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventorySorter {
    private static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    private static final int MAIN_INVENTORY_SIZE = 27;
    private static final BundleTargetProvider HOTBAR_BUNDLE_TARGETS = new HotbarBundleTargetProvider();

    private PlayerInventorySorter() {
    }

    public static void sort(ServerPlayer player, SortSettings settings, String languageCode) {
        if (!settings.sortIntoBundles()) {
            ContainerInventorySorter.sort(
                    player.getInventory(),
                    FIRST_MAIN_INVENTORY_SLOT,
                    MAIN_INVENTORY_SIZE,
                    settings.sortType(),
                    languageCode,
                    settings.sortPriorityRules(),
                    false
            );
            return;
        }

        List<ItemStack> mainInventoryStacks = ContainerStacks.get(
                player.getInventory(),
                FIRST_MAIN_INVENTORY_SLOT,
                MAIN_INVENTORY_SIZE
        );
        BundleTargetSlots extraBundleTargets = BundleTargetSlots.fromSlots(extraBundleSlots(player, settings));
        BundleInsertionLayoutPass.Result bundleInsertion = BundleInsertionLayoutPass.apply(
                mainInventoryStacks,
                extraBundleTargets.stacks(),
                SortPriorityRules.compile(settings.sortPriorityRules())
        );
        SortedInventoryLayout sortedInventoryLayout = SortedInventoryLayout.fromBundleAdjusted(
                bundleInsertion.layoutStacks(),
                settings.sortType(),
                languageCode,
                settings.sortPriorityRules()
        );

        extraBundleTargets.setStacks(bundleInsertion.extraTargetStacks());
        ContainerStacks.set(player.getInventory(), FIRST_MAIN_INVENTORY_SLOT, sortedInventoryLayout.stacks());
        player.getInventory().setChanged();
    }

    private static List<BundleTargetSlot> extraBundleSlots(ServerPlayer player, SortSettings settings) {
        List<BundleTargetSlot> slots = new ArrayList<>();
        slots.addAll(HOTBAR_BUNDLE_TARGETS.slots(player, settings));
        slots.addAll(CompatibilityPlugins.serverBundleSlots(player, settings));
        return List.copyOf(slots);
    }
}
