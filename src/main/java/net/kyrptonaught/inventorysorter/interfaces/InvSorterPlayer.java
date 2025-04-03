package net.kyrptonaught.inventorysorter.interfaces;

import net.kyrptonaught.inventorysorter.SortCases;
import net.minecraft.server.network.ServerPlayerEntity;

public interface InvSorterPlayer {
    SortCases.SortType getSortType();

    void setSortType(SortCases.SortType sortType);

    boolean getMiddleClick();

    void setMiddleClick(boolean middleClick);

    boolean getDoubleClickSort();

    void setDoubleClickSort(boolean doubleClick);

    void syncSettings(ServerPlayerEntity player);
}
