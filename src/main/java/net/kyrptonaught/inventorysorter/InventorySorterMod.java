package net.kyrptonaught.inventorysorter;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
//? }
import net.kyrptonaught.inventorysorter.client.platform.neoforge.NeoForgeConfigScreenFactory;
import net.kyrptonaught.inventorysorter.compat.Compatibility;
import net.kyrptonaught.inventorysorter.compat.sources.*;
import net.kyrptonaught.inventorysorter.config.Config;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
//? if neoforge {
/*import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///? }
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//? if neoforge
//@Mod(InventorySorterMod.MOD_ID)
public class InventorySorterMod /*? if fabric {*/implements ModInitializer/*?}*/ {
    public static final String MOD_ID = "inventorysorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    //? if fabric
    private final ServerPlayerJoinSync playerJoinSync = new ServerPlayerJoinSync();
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

    //? if neoforge {
    /*public InventorySorterMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, new NeoForgeConfigScreenFactory());
    }
    *///?}

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    public static void reloadConfig() {
        CONFIG = Config.load();
        compatibility.reload();
    }

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        PlatformServices.COMMANDS.registerCommands();

        PlatformServices.NETWORK.registerPayloads();

        ServerLifecycleEvents.SERVER_STARTED.register(this::ensureCreativeSearchTabsBuilt);

        ServerPlayConnectionEvents.JOIN.register((handler, server, client) -> playerJoinSync.syncJoiningPlayer(handler.getPlayer(), client));

    }

    void ensureCreativeSearchTabsBuilt(MinecraftServer server) {
        var context = new CreativeModeTab.ItemDisplayParameters(server.getWorldData().enabledFeatures(), false, server.registryAccess());
        CreativeModeTabs.allTabs().forEach(group -> {
            if (group.getSearchTabDisplayItems().isEmpty()) group.buildContents(context);
        });
    }
    /*?}*/
}
