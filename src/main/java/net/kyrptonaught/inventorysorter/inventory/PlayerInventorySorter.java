package net.kyrptonaught.inventorysorter.inventory;

import java.util.List;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleInsertionLayoutPass;
import net.kyrptonaught.inventorysorter.inventory.bundle.BundleTargetProvider;
import net.kyrptonaught.inventorysorter.sort.bundle.BundleTargetSlots;
import net.kyrptonaught.inventorysorter.inventory.bundle.CompositeBundleTargetProvider;
import net.kyrptonaught.inventorysorter.inventory.bundle.HotbarBundleTargetProvider;
import net.kyrptonaught.inventorysorter.inventory.bundle.TrinketsBundleTargetProvider;
import net.kyrptonaught.inventorysorter.inventory.container.ContainerStacks;
import net.kyrptonaught.inventorysorter.sort.SortedInventoryLayout;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventorySorter {
    private static final int FIRST_MAIN_INVENTORY_SLOT = 9;
    private static final int MAIN_INVENTORY_SIZE = 27;
    private static final BundleTargetProvider BUNDLE_TARGETS = new CompositeBundleTargetProvider(List.of(
            new HotbarBundleTargetProvider(),
            new TrinketsBundleTargetProvider()
    ));

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
        BundleTargetSlots extraBundleTargets = BundleTargetSlots.fromContainers(BUNDLE_TARGETS.containers(player, settings));
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
}
