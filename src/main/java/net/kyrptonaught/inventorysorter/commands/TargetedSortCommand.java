package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.compat.ServerComponent;

import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.screen.TargetedInventoryResolver;
import net.kyrptonaught.inventorysorter.inventory.ContainerInventorySorter;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TargetedSortCommand {
    private TargetedSortCommand() {
    }

    public static Component sort(ServerPlayer player, SortSettings settings) {
        Boolean result = TargetedInventoryResolver.withTargetedScreen(player, context -> {
            if (context.inventory() == null) {
                return false;
            }
            if (SortabilityPolicy.canSortInventory(player, context.menu())) {
                String languageCode = player.clientInformation().language().toLowerCase();
                ContainerInventorySorter.sort(
                        context.inventory(),
                        0,
                        context.inventory().getContainerSize(),
                        settings.sortType(),
                        languageCode,
                        settings.sortPriorityRules(),
                        settings.sortIntoBundles()
                );
                return true;
            }
            return false;
        });

        if (result == null) {
            return ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.sort.error");
        }
        if (result) {
            return ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.sort.sorted");
        }

        return ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.cmd.sort.notsortable");
    }
}
