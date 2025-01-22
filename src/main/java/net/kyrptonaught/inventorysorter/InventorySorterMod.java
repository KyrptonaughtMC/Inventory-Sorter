package net.kyrptonaught.inventorysorter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.inventorysorter.client.config.IgnoreList;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.kyrptonaught.inventorysorter.network.SyncInvSortSettingsPacket;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InventorySorterMod implements ModInitializer {
    public static final String MOD_ID = "inventorysorter";
    private static IgnoreList ignoreList = new IgnoreList();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(SortCommand::register);
        InventorySortPacket.registerReceivePacket();
        SyncInvSortSettingsPacket.registerReceiveSyncData();
        SyncBlacklistPacket.registerSyncOnPlayerJoin();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var context = new ItemGroup.DisplayContext(server.getSaveProperties().getEnabledFeatures(), false, server.getRegistryManager());
            ItemGroups.getGroups().forEach(group -> {
                if (group.getSearchTabStacks().isEmpty()) group.updateEntries(context);
            });
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer instanceof InvSorterPlayer) {
                ((InvSorterPlayer) newPlayer).setSortType(((InvSorterPlayer) oldPlayer).getSortType());
                ((InvSorterPlayer) newPlayer).setMiddleClick(((InvSorterPlayer) oldPlayer).getMiddleClick());
            }
        });
    }

    public static IgnoreList getBlackList() {
        return ignoreList;
//        return (IgnoreList) configManager.getConfig("blacklist.json5");
    }
}
