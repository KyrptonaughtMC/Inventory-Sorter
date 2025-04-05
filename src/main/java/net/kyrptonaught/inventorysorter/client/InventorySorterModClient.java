package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    public static final KeyBinding sortButton = new KeyBinding(
            "inventorysorter.keybinds.sort",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.inventorysorter"
    );

    public static final KeyBinding configButton = new KeyBinding(
            "inventorysorter.keybinds.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.inventorysorter"
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(sortButton);
        KeyBindingHelper.registerKeyBinding(configButton);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncConfig());

        SyncBlacklistPacket.registerReceiveBlackList();

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (sortButton.wasPressed()) {
                client.setScreen(ConfigScreen.getConfigeScreen(null));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> {
            NewConfigOptions currentConfig = getConfig();
            currentConfig.enableMiddleClickSort = payload.enableMiddleClick();
            currentConfig.enableDoubleClickSort = payload.enableDoubleClick();
            currentConfig.sortType = payload.sortType();
            currentConfig.save();
        });

        ClientPlayNetworking.registerGlobalReceiver(PlayerSortPrevention.ID, (payload, context) -> {
            LOGGER.info("Received sort prevention packet");
            NewConfigOptions currentConfig = getConfig();
            currentConfig.preventSortForScreens.retainAll(payload.preventSortForScreens());
            payload.preventSortForScreens().forEach(currentConfig::disableSortForScreen);
            currentConfig.save();
            compatibility.reload();
        });
    }

    public static void syncConfig() {
        NewConfigOptions config = getConfig();

        ClientPlayNetworking.send(SortSettings.fromConfig(config));
        ClientPlayNetworking.send(PlayerSortPrevention.fromConfig(config));
    }

    public static boolean isKeybindPressed(int pressedKeyCode, int scanCode, InputUtil.Type type) {
        return switch (type) {
            case KEYSYM -> sortButton.matchesKey(pressedKeyCode, scanCode);
            case MOUSE -> sortButton.matchesMouse(pressedKeyCode);
            default -> false;
        };
    }
}
