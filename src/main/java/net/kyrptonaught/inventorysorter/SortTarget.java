package net.kyrptonaught.inventorysorter;

public enum SortTarget {
    CONTAINER(false),
    PLAYER_INVENTORY(true);

    private final boolean playerInventory;

    SortTarget(boolean playerInventory) {
        this.playerInventory = playerInventory;
    }

    public static SortTarget fromPlayerInventory(boolean playerInventory) {
        return playerInventory ? PLAYER_INVENTORY : CONTAINER;
    }

    public boolean isPlayerInventory() {
        return playerInventory;
    }
}
