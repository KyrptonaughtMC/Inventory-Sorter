/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.platform.neoforge;

import net.kyrptonaught.inventorysorter.ServerPlayerJoinSync;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class NeoForgeServerEvents {
    private static final ServerPlayerJoinSync PLAYER_JOIN_SYNC = new ServerPlayerJoinSync();

    private NeoForgeServerEvents() {
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        var context = new CreativeModeTab.ItemDisplayParameters(server.getWorldData().enabledFeatures(), false, server.registryAccess());
        CreativeModeTabs.allTabs().forEach(group -> {
            if (group.getSearchTabDisplayItems().isEmpty()) group.buildContents(context);
        });
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.level().getServer();
            if (server != null) {
                PLAYER_JOIN_SYNC.syncJoiningPlayer(player, server);
            }
        }
    }
}
*//*?}*/
