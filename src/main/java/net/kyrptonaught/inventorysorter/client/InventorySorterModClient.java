package net.kyrptonaught.inventorysorter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

import com.mojang.blaze3d.platform.InputConstants;

public class InventorySorterModClient implements ClientModInitializer {

    private CompatConfig serverConfig = new CompatConfig();
    private volatile boolean serverIsPresent = false;
    private ScheduledExecutorService scheduler;
    public static Identifier PLAYER_INVENTORY = Identifier.parse("player_inventory");
    private static final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "main"));

    public static final KeyMapping configButton = new KeyMapping(
            "inventorysorter.key.config",
            InputConstants.KEY_P,
            category
    );

    public static final KeyMapping sortButton = new KeyMapping(
            "inventorysorter.key.sort",
            InputConstants.KEY_P,
            category
    );

    public static final InputConstants.Key modifierButton = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LCONTROL);


    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownScheduler));

        KeyBindingHelper.registerKeyBinding(configButton);
        KeyBindingHelper.registerKeyBinding(sortButton);

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(() -> serverConfig));



        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            serverIsPresent = false;
            scheduler = Executors.newSingleThreadScheduledExecutor();

            ClientPlayNetworking.send(new ClientSync(true));
            syncConfig();

            // Two-stage check: first at 5 seconds, then at 25 seconds if still no server
            scheduler.schedule(() -> {
                if (!serverIsPresent) {
                    // First check at 5 seconds - schedule another check at 25 seconds
                    scheduler.schedule(() -> {
                        if (!serverIsPresent && client.player != null) {
                            client.execute(() -> client.player.displayClientMessage(
                                    Component.literal("[Inventory Sorter] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA))
                                            .append(Component.translatable("inventorysorter.warning.missing-server").withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                                            ), false));
                        }
                    }, 20, TimeUnit.SECONDS);
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

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            /*
                Using this in favor of injecting into the screen mouse scroll event due to mod compatibility issues.
                Some mods completely override the mouse scroll event, which can cause issues with the sort button.
                This way, we ensure that the sort button's scroll functionality is always checked after the screen is initialized.
            */
            ScreenMouseEvents.afterMouseScroll(screen).register((scr, x, y, horizontalAmount, verticalAmount, consumed) -> {
                if (!(scr instanceof SortableContainerScreen innerScreen)) {
                    // If it's not our screen type, we don't handle the scroll event.
                    return false;
                }

                SortButtonWidget inventoryButton = innerScreen.inventorySorter$getSortButton();
                if (inventoryButton != null && inventoryButton.visible && inventoryButton.isHovered()) {
                    inventoryButton.mouseScrolled(x, y, verticalAmount, horizontalAmount);
                }

                SortButtonWidget playerButton = innerScreen.inventorySorter$getPlayerSortButton();
                if (playerButton != null && playerButton.visible && playerButton.isHovered()) {
                    playerButton.mouseScrolled(x, y, verticalAmount, horizontalAmount);
                }
                return true;
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            InputConstants.Key config = KeyBindingHelper.getBoundKeyOf(configButton);
            InputConstants.Key sort = KeyBindingHelper.getBoundKeyOf(sortButton);
            Supplier<Boolean> keyToCheck = configButton::consumeClick;

            if (config.getValue() == sort.getValue()) {
                keyToCheck = () -> sortButton.consumeClick() || configButton.consumeClick();
            }

            if (keyToCheck.get()) {
                client.setScreen(ConfigScreen.getConfigScreen(client.screen));
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
            Minecraft client = context.client();
            if (payload.lastSeenVersion().equals(VERSION) && payload.lastSeenLanguage().equals(client.getLanguageManager().getSelected().toLowerCase())) {
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
