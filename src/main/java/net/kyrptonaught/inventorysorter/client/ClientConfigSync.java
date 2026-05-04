package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public class ClientConfigSync {
    public static void syncConfigToServer() {
        syncConfigToServer(getConfig(), PlatformServices.NETWORK);
    }

    public static void syncConfigToServer(NewConfigOptions config, NetworkingPlatform networking) {
        networking.sendToServer(SortSettings.fromConfig(config));
        networking.sendToServer(PlayerSortPrevention.fromConfig(config));
    }
}
