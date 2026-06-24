package net.kyrptonaught.inventorysorter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SortTargetTest {
    @Test
    void convertsLegacyPlayerInventoryFlagAtPacketBoundary() {
        Assertions.assertEquals(SortTarget.PLAYER_INVENTORY, SortTarget.fromPlayerInventory(true));
        Assertions.assertEquals(SortTarget.CONTAINER, SortTarget.fromPlayerInventory(false));
    }

    @Test
    void exposesLegacyPlayerInventoryFlagForPacketEncoding() {
        Assertions.assertTrue(SortTarget.PLAYER_INVENTORY.isPlayerInventory());
        Assertions.assertFalse(SortTarget.CONTAINER.isPlayerInventory());
    }
}
