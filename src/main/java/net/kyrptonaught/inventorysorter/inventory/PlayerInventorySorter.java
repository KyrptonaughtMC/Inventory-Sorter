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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventorySorter {
    private static final BundleTargetProvider HOTBAR_BUNDLE_TARGETS = new HotbarBundleTargetProvider();

    private PlayerInventorySorter() {
    }

    public static void sort(ServerPlayer player, SortSettings settings, String languageCode) {
        Inventory inventory = player.getInventory();
        int firstMainInventorySlot = Inventory.getSelectionSize();
        int mainInventorySize = inventory.getNonEquipmentItems().size() - firstMainInventorySlot;
        if (!settings.sortIntoBundles()) {
            ContainerInventorySorter.sort(
                    player.getInventory(),
                    firstMainInventorySlot,
                    mainInventorySize,
                    settings.sortType(),
                    languageCode,
                    settings.sortPriorityRules(),
                    false
            );
            return;
        }

        List<ItemStack> mainInventoryStacks = ContainerStacks.get(
                player.getInventory(),
                firstMainInventorySlot,
                mainInventorySize
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
        ContainerStacks.set(inventory, firstMainInventorySlot, sortedInventoryLayout.stacks());
        player.getInventory().setChanged();
    }

    private static List<BundleTargetSlot> extraBundleSlots(ServerPlayer player, SortSettings settings) {
        List<BundleTargetSlot> slots = new ArrayList<>();
        slots.addAll(HOTBAR_BUNDLE_TARGETS.slots(player, settings));
        slots.addAll(CompatibilityPlugins.serverBundleSlots(player, settings));
        return List.copyOf(slots);
    }
}
