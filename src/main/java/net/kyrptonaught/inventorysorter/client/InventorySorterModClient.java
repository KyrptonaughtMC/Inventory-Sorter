package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    private CompatConfig serverConfig = new CompatConfig();

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

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(serverConfig));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(new ClientSync(true));
            syncConfig();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            /*
              This is to clear the server defined configs when the client disconnects from a server.
              This is to prevent configs from one server from being used on another server.
             */
            serverConfig = new CompatConfig();
            compatibility.reload();
        });

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

        /*
          This happens when the client connects to a server for the first time.
          It's to sync the server's config to the client if the user has added any sort
          preventions for themselves.
         */
        ClientPlayNetworking.registerGlobalReceiver(PlayerSortPrevention.ID, (payload, context) -> {
            NewConfigOptions currentConfig = getConfig();
            currentConfig.preventSortForScreens.retainAll(payload.preventSortForScreens());
            payload.preventSortForScreens().forEach(currentConfig::disableSortForScreen);
            currentConfig.save();
            compatibility.reload();
        });

        /*
          If the server owners have defined any screens that should have the sort button hidden,
          this is how we sync that to the client and keep it separate from the player's config.
         */
        ClientPlayNetworking.registerGlobalReceiver(HideButton.ID, (payload, context) -> {
            serverConfig.hideButtonsForScreens = payload.hideButtonForScreens().stream().toList();
            compatibility.reload();
        });

        ClientPlayNetworking.registerGlobalReceiver(ReloadConfigPacket.ID, (payload, context) -> {
            reloadConfig();
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
