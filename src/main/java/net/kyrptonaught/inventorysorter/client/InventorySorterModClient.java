package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.client.config.Config;
import net.kyrptonaught.inventorysorter.client.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.kyrptonaught.inventorysorter.network.SyncInvSortSettingsPacket;
import net.minecraft.client.util.InputUtil;

public class InventorySorterModClient implements ClientModInitializer {

    private static final NewConfigOptions CONFIG = Config.load();

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncConfig());
        SyncBlacklistPacket.registerReceiveBlackList();
    }

    public static void syncConfig() {
        SyncInvSortSettingsPacket.registerSyncOnPlayerJoin();
    }

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    public static boolean isKeybindPressed(int pressedKeyCode, InputUtil.Type type) {
        return false;
        //@TODO: Fix this
        //return getConfig().keybinding.matches(pressedKeyCode, type);
    }
}
