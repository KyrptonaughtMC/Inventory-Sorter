package net.kyrptonaught.inventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class InventorySorterModClient implements ClientModInitializer {

    public static final InputConstants.Key modifierButton = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LCONTROL);
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
    public static Identifier PLAYER_INVENTORY = Identifier.parse("player_inventory");
    private CompatConfig serverConfig = new CompatConfig();
    private volatile boolean serverIsPresent = false;
    private ScheduledExecutorService scheduler;

    public static void syncConfig() {
        ClientConfigSync.syncConfigToServer(getConfig(), PlatformServices.NETWORK);
    }

    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownScheduler));

        KeyMappingHelper.registerKeyMapping(configButton);
        KeyMappingHelper.registerKeyMapping(sortButton);

        /*
          This is to attach server defined configs to the compatibility layer on the client only
         */
        compatibility.addLoader(new ConfigLoader(() -> serverConfig));


        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            handleClientJoin(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            /*
              This is to clear the server defined configs when the client disconnects from a server.
              This is to prevent configs from one server from being used on another server.
             */
            resetServerStateOnDisconnect();
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            InputConstants.Key config = KeyMappingHelper.getBoundKeyOf(configButton);
            InputConstants.Key sort = KeyMappingHelper.getBoundKeyOf(sortButton);
            ConfigScreen.openIfConfigKeyPressed(client, configButton, sortButton, config, sort);
        });

        registerClientReceivers();
    }

    private void registerClientReceivers() {
        PlatformServices.NETWORK.registerClientReceivers(
                this::applySortSettings,
                this::applyPlayerSortPrevention,
                this::applyHideButton,
                InventorySorterMod::reloadConfig,
                this::handleLastSeenVersion,
                this::handleServerPresence
        );
    }

    private void applySortSettings(SortSettings payload) {
        NewConfigOptions currentConfig = getConfig();
        currentConfig.enableDoubleClickSort = payload.enableDoubleClick();
        currentConfig.sortType = payload.sortType();
        currentConfig.save();
    }

    /*
      This happens when the client connects to a server for the first time.
      It's to sync the server's config to the client if the user has added any sort
      preventions for themselves.
     */
    private void applyPlayerSortPrevention(PlayerSortPrevention payload) {
        NewConfigOptions currentConfig = getConfig();
        currentConfig.preventSortForScreens.retainAll(payload.preventSortForScreens());
        payload.preventSortForScreens().forEach(currentConfig::disableSortForScreen);
        currentConfig.save();
        compatibility.reload();
    }

    /*
      If the server owners have defined any screens that should have the sort button hidden,
      this is how we sync that to the client and keep it separate from the player's config.
     */
    private void applyHideButton(HideButton payload) {
        serverConfig.hideButtonsForScreens = payload.hideButtonForScreens().stream().toList();
        compatibility.reload();
    }

    private void handleLastSeenVersion(LastSeenVersionPacket payload) {
        Minecraft client = Minecraft.getInstance();
        if (payload.lastSeenVersion().equals(VERSION) && payload.lastSeenLanguage().equals(client.getLanguageManager().getSelected().toLowerCase())) {
            return;
        }
        TranslationReminder.notify(client);
    }

    private void handleServerPresence() {
        serverIsPresent = true;
    }

    private void handleClientJoin(Minecraft client) {
        serverIsPresent = false;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        PlatformServices.NETWORK.sendToServer(new ClientSync(true));
        syncConfig();

        scheduleMissingServerWarning(client);
    }

    private void scheduleMissingServerWarning(Minecraft client) {
        // Two-stage check: first at 5 seconds, then at 25 seconds if still no server
        scheduler.schedule(() -> {
            if (!serverIsPresent) {
                // First check at 5 seconds - schedule another check at 25 seconds
                scheduler.schedule(() -> {
                    if (!serverIsPresent && client.player != null) {
                        client.execute(() -> client.player.sendSystemMessage(
                                Component.literal("[Inventory Sorter] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA))
                                        .append(Component.translatable("inventorysorter.warning.missing-server").withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                                        )));
                    }
                }, 20, TimeUnit.SECONDS);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void resetServerStateOnDisconnect() {
        serverConfig = new CompatConfig();
        compatibility.reload();
        serverIsPresent = false;
        shutdownScheduler();
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
