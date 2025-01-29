package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.compat.Compatibility;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.compat.sources.LocalLoader;
import net.kyrptonaught.inventorysorter.compat.sources.OfficialListLoader;
import net.kyrptonaught.inventorysorter.client.config.Config;
import net.kyrptonaught.inventorysorter.client.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.kyrptonaught.inventorysorter.network.SyncInvSortSettingsPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class InventorySorterModClient implements ClientModInitializer {

    public static final KeyBinding sortButton = new KeyBinding(
            "inventorysorter.keybinds.sort",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.inventorysorter"
    );

    private static final NewConfigOptions CONFIG = Config.load();
    public static final Compatibility compatibility = new Compatibility(
            List.of(
                    new LocalLoader(),
                    new OfficialListLoader(),
                    new ConfigLoader(CONFIG)
            )
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(sortButton);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncConfig());
        SyncBlacklistPacket.registerReceiveBlackList();
    }

    public static void syncConfig() {
        SyncInvSortSettingsPacket.registerSyncOnPlayerJoin();
    }

    public static boolean isKeybindPressed(int pressedKeyCode, int scanCode, InputUtil.Type type) {
        return switch (type) {
            case KEYSYM -> sortButton.matchesKey(pressedKeyCode, scanCode);
            case MOUSE -> sortButton.matchesMouse(pressedKeyCode);
            default -> false;
        };
    }

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }
}
