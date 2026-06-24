package net.kyrptonaught.inventorysorter.platform;

import net.kyrptonaught.inventorysorter.network.ClientSync;
import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;
import net.kyrptonaught.inventorysorter.network.PlayerSortPrevention;
import net.kyrptonaught.inventorysorter.network.SortSettings;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerDataPlatform {
    SortSettings getSortSettings(ServerPlayer player);

    void setSortSettings(ServerPlayer player, SortSettings settings);

    PlayerSortPrevention getPlayerSortPrevention(ServerPlayer player);

    void setPlayerSortPrevention(ServerPlayer player, PlayerSortPrevention value);

    ClientSync getClientSync(ServerPlayer player);

    void setClientSync(ServerPlayer player, ClientSync value);

    LastSeenVersionPacket getLastSeenVersion(ServerPlayer player);

    void setLastSeenVersion(ServerPlayer player, LastSeenVersionPacket value);
}
