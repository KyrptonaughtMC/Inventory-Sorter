package net.kyrptonaught.inventorysorter.client;

public interface SortableContainerScreen {
    SortButtonWidget inventorySorter$getSortButton();
    SortButtonWidget inventorySorter$getPlayerSortButton();

    int getMiddleHeight();
}
