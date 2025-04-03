package net.kyrptonaught.inventorysorter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.commands.CommandRegistry;
import net.kyrptonaught.inventorysorter.compat.Compatibility;
import net.kyrptonaught.inventorysorter.compat.sources.ConfigLoader;
import net.kyrptonaught.inventorysorter.compat.sources.LocalLoader;
import net.kyrptonaught.inventorysorter.compat.sources.OfficialListLoader;
import net.kyrptonaught.inventorysorter.config.Config;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.kyrptonaught.inventorysorter.network.InventorySortPacket;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.network.SyncBlacklistPacket;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class InventorySorterMod implements ModInitializer {
    public static final String MOD_ID = "inventorysorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final NewConfigOptions CONFIG = Config.load();
    public static final Compatibility compatibility = new Compatibility(
            List.of(
                    new LocalLoader(),
                    new OfficialListLoader(),
                    new ConfigLoader(CONFIG)
            )
    );

    public static NewConfigOptions getConfig() {
        return CONFIG;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);

        PayloadTypeRegistry.playC2S().register(SortSettings.ID, SortSettings.CODEC);
        PayloadTypeRegistry.playS2C().register(SortSettings.ID, SortSettings.CODEC);

        InventorySortPacket.registerReceivePacket();
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

        ServerPlayNetworking.registerGlobalReceiver(SortSettings.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            player.getServer().execute(() -> {
                ((InvSorterPlayer) player).setMiddleClick(payload.enableMiddleClick());
                ((InvSorterPlayer) player).setDoubleClickSort(payload.enableDoubleClick());
                ((InvSorterPlayer) player).setSortType(payload.sortType());
            });
        });
    }
}
