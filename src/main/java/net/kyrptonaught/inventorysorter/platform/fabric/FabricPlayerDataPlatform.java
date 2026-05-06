package net.kyrptonaught.inventorysorter.platform.fabric;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.kyrptonaught.inventorysorter.platform.PlayerDataPlatform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class FabricPlayerDataPlatform implements PlayerDataPlatform {
    @SuppressWarnings("UnstableApiUsage")
    private static final AttachmentType<SortSettings> SORT_SETTINGS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "sort_settings"),
            builder -> builder
                    .initializer(() -> SortSettings.DEFAULT)
                    .persistent(SortSettings.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    private static final AttachmentType<PlayerSortPrevention> PLAYER_SORT_PREVENTION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "player_sort_prevention"),
            builder -> builder
                    .initializer(() -> PlayerSortPrevention.DEFAULT)
                    .persistent(PlayerSortPrevention.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    private static final AttachmentType<ClientSync> CLIENT_SYNC = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "client_sync"),
            builder -> builder
                    .initializer(() -> ClientSync.DEFAULT)
                    .persistent(ClientSync.NBT_CODEC)
                    .copyOnDeath()
    );

    @SuppressWarnings("UnstableApiUsage")
    private static final AttachmentType<LastSeenVersionPacket> LAST_SEEN_VERSION = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "last_seen_version"),
            builder -> builder
                    .initializer(() -> LastSeenVersionPacket.DEFAULT)
                    .persistent(LastSeenVersionPacket.NBT_CODEC)
    );

    @Override
    public SortSettings getSortSettings(ServerPlayer player) {
        return player.getAttachedOrCreate(SORT_SETTINGS);
    }

    @Override
    public void setSortSettings(ServerPlayer player, SortSettings settings) {
        player.setAttached(SORT_SETTINGS, settings);
    }

    @Override
    public PlayerSortPrevention getPlayerSortPrevention(ServerPlayer player) {
        return player.getAttachedOrCreate(PLAYER_SORT_PREVENTION);
    }

    @Override
    public void setPlayerSortPrevention(ServerPlayer player, PlayerSortPrevention value) {
        player.setAttached(PLAYER_SORT_PREVENTION, value);
    }

    @Override
    public ClientSync getClientSync(ServerPlayer player) {
        return player.getAttachedOrCreate(CLIENT_SYNC);
    }

    @Override
    public void setClientSync(ServerPlayer player, ClientSync value) {
        player.setAttached(CLIENT_SYNC, value);
    }

    @Override
    public LastSeenVersionPacket getLastSeenVersion(ServerPlayer player) {
        return player.getAttachedOrCreate(LAST_SEEN_VERSION);
    }

    @Override
    public void setLastSeenVersion(ServerPlayer player, LastSeenVersionPacket value) {
        player.setAttached(LAST_SEEN_VERSION, value);
    }
}
