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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    private CompatConfig serverConfig = new CompatConfig();
    private volatile boolean serverIsPresent = false;
    private ScheduledExecutorService scheduler;


    public static final KeyBinding configButton = new KeyBinding(
            "inventorysorter.key.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "inventorysorter.key.category"
    );

    public static final KeyBinding sortButton = new KeyBinding(
            "inventorysorter.key.sort",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "inventorysorter.key.category"
    );

    public static final KeyBinding modifierButton = new KeyBinding(
            "inventorysorter.key.modifier",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "inventorysorter.key.category"
    );

    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownScheduler));

        KeyBindingHelper.registerKeyBinding(configButton);
        KeyBindingHelper.registerKeyBinding(sortButton);
        KeyBindingHelper.registerKeyBinding(modifierButton);

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(() -> serverConfig));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            scheduler = Executors.newSingleThreadScheduledExecutor();

            ClientPlayNetworking.send(new ClientSync(true));
            syncConfig();

            scheduler.schedule(() -> {
                if (!serverIsPresent) {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.sendMessage(
                                    Text.literal("[Inventory Sorter] ").styled(style -> style.withBold(true).withColor(Formatting.AQUA))
                                            .append(Text.translatable("inventorysorter.warning.missing-server").styled(style -> style.withBold(false).withColor(Formatting.YELLOW))
                                            ), false);
                        }
                    });
                }
            }, 5, TimeUnit.SECONDS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            /*
              This is to clear the server defined configs when the client disconnects from a server.
              This is to prevent configs from one server from being used on another server.
             */
            serverConfig = new CompatConfig();
            compatibility.reload();
            serverIsPresent = false;
            shutdownScheduler();
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            InputUtil.Key config = KeyBindingHelper.getBoundKeyOf(configButton);
            InputUtil.Key sort = KeyBindingHelper.getBoundKeyOf(sortButton);
            Supplier<Boolean> keyToCheck = configButton::wasPressed;

            if (config.getCode() == sort.getCode()) {
                keyToCheck = () -> sortButton.wasPressed() || configButton.wasPressed();
            }

            if (keyToCheck.get()) {
                client.setScreen(ConfigScreen.getConfigScreen(client.currentScreen));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> {
            NewConfigOptions currentConfig = getConfig();
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

        ClientPlayNetworking.registerGlobalReceiver(LastSeenVersionPacket.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            if (payload.lastSeenVersion().equals(VERSION) && payload.lastSeenLanguage().equals(client.getLanguageManager().getLanguage().toLowerCase())) {
                return;
            }
            TranslationReminder.notify(client);
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerPresencePacket.ID, (payload, context) -> {
            serverIsPresent = true;
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

    private void shutdownScheduler() {
        if (scheduler == null || scheduler.isShutdown()) return;

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
