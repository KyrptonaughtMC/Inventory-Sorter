/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.platform.neoforge;

import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlayerDataPlatform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class NeoForgePlayerDataPlatform implements PlayerDataPlatform {
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<SortSettings>> SORT_SETTINGS = attachment("sort_settings");
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSortPrevention>> PLAYER_SORT_PREVENTION = attachment("player_sort_prevention");
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<ClientSync>> CLIENT_SYNC = attachment("client_sync");
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<LastSeenVersionPacket>> LAST_SEEN_VERSION = attachment("last_seen_version");

    @SubscribeEvent
    public static void registerAttachments(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, id("sort_settings"), () -> AttachmentType
                .builder(() -> SortSettings.DEFAULT)
                .serialize(SortSettings.NBT_CODEC.fieldOf("value"))
                .copyOnDeath()
                .build());
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, id("player_sort_prevention"), () -> AttachmentType
                .builder(() -> PlayerSortPrevention.DEFAULT)
                .serialize(PlayerSortPrevention.NBT_CODEC.fieldOf("value"))
                .copyOnDeath()
                .build());
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, id("client_sync"), () -> AttachmentType
                .builder(() -> ClientSync.DEFAULT)
                .serialize(ClientSync.NBT_CODEC.fieldOf("value"))
                .copyOnDeath()
                .build());
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, id("last_seen_version"), () -> AttachmentType
                .builder(() -> LastSeenVersionPacket.DEFAULT)
                .serialize(LastSeenVersionPacket.NBT_CODEC.fieldOf("value"))
                .build());
    }

    @Override
    public SortSettings getSortSettings(ServerPlayer player) {
        return player.getData(SORT_SETTINGS);
    }

    @Override
    public void setSortSettings(ServerPlayer player, SortSettings settings) {
        player.setData(SORT_SETTINGS, settings);
    }

    @Override
    public PlayerSortPrevention getPlayerSortPrevention(ServerPlayer player) {
        return player.getData(PLAYER_SORT_PREVENTION);
    }

    @Override
    public void setPlayerSortPrevention(ServerPlayer player, PlayerSortPrevention value) {
        player.setData(PLAYER_SORT_PREVENTION, value);
    }

    @Override
    public ClientSync getClientSync(ServerPlayer player) {
        return player.getData(CLIENT_SYNC);
    }

    @Override
    public void setClientSync(ServerPlayer player, ClientSync value) {
        player.setData(CLIENT_SYNC, value);
    }

    @Override
    public LastSeenVersionPacket getLastSeenVersion(ServerPlayer player) {
        return player.getData(LAST_SEEN_VERSION);
    }

    @Override
    public void setLastSeenVersion(ServerPlayer player, LastSeenVersionPacket value) {
        player.setData(LAST_SEEN_VERSION, value);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> attachment(String path) {
        return DeferredHolder.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, id(path));
    }
}
*//*?}*/
