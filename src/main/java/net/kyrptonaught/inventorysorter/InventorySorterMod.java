package net.kyrptonaught.inventorysorter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.inventorysorter.compat.Compatibility;
import net.kyrptonaught.inventorysorter.compat.sources.*;
import net.kyrptonaught.inventorysorter.config.Config;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class InventorySorterMod implements ModInitializer {
    public static final String MOD_ID = "inventorysorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static NewConfigOptions CONFIG = Config.load();
    public static final Compatibility compatibility = new Compatibility(
            new ArrayList<>(List.of(
                    new PredefinedLoader(),
                    new LocalLoader(),
                    new OfficialListLoader(),
                    new ConfigLoader(InventorySorterMod::getConfig),
                    new RemoteConfigLoader(() -> InventorySorterMod.getConfig().customCompatibilityListDownloadUrl)
            ))
    );
    public static final String VERSION = "VERSION_REPL";

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    public static void reloadConfig() {
        CONFIG = Config.load();
        compatibility.reload();
    }

    @Override
    public void onInitialize() {
        PlatformServices.COMMANDS.registerCommands();

        PlatformServices.NETWORK.registerPayloads();

        ServerLifecycleEvents.SERVER_STARTED.register(this::ensureCreativeSearchTabsBuilt);

        ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> {
            ServerPlayer player = handler.getPlayer();
            PlatformServices.NETWORK.sendToPlayer(player, new ServerPresencePacket());
            PlatformServices.PLAYER_DATA.getLastSeenVersion(player).send(player);

            PlatformServices.PLAYER_DATA.setLastSeenVersion(player, new LastSeenVersionPacket(VERSION, player.clientInformation().language().toLowerCase()));

            if (PlatformServices.PLATFORM.isDedicatedServer(client)) {
                if (!PlatformServices.PLAYER_DATA.getClientSync(player).seenClient()) {
                /*
                  If we haven't seen the client MOD before, we need to send the config we have for the player.
                  This is for the case when a player hasn't used the mod on the client before but has settings stored
                  for them on the server.

                  When the client connects for the first time, we send them the config we have for them.
                 */
                    PlayerSortPrevention sortPrevention = PlatformServices.PLAYER_DATA.getPlayerSortPrevention(player);
                    if (sortPrevention != PlayerSortPrevention.DEFAULT) {
                        sortPrevention.sync(player);
                    }

                    SortSettings sortSettings = PlatformServices.PLAYER_DATA.getSortSettings(player);
                    if (sortSettings != SortSettings.DEFAULT) {
                        sortSettings.sync(player);
                    }
                }
                HideButton.fromConfig(getConfig()).sync(player);
            }
        });

    }

    void ensureCreativeSearchTabsBuilt(MinecraftServer server) {
        var context = new CreativeModeTab.ItemDisplayParameters(server.getWorldData().enabledFeatures(), false, server.registryAccess());
        CreativeModeTabs.allTabs().forEach(group -> {
            if (group.getSearchTabDisplayItems().isEmpty()) group.buildContents(context);
        });
    }
}
