package net.kyrptonaught.inventorysorter.compat;

import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

final class TrinketsApiBundleTargets {
    private TrinketsApiBundleTargets() {
    }

    static List<Container> containers(Player player) {
        return TrinketsApi.getAttachment(player)
                .getInventories()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .filter(container -> container.getContainerSize() > 0)
                .map(Container.class::cast)
                .toList();
    }

    static boolean isTrinketInventory(Container container) {
        return container instanceof TrinketInventory;
    }
}
