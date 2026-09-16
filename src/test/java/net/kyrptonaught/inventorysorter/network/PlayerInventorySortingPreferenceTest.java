package net.kyrptonaught.inventorysorter.network;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.inventory.SortabilityPolicy;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInventorySortingPreferenceTest {
    @Test
    void missingConfigAndPlayerDataKeepPlayerSortingEnabled() {
        assertTrue(new Gson().fromJson("{}", NewConfigOptions.class).allowPlayerInventorySorting);
        assertFalse(new Gson().fromJson("{\"allowPlayerInventorySorting\":false}", NewConfigOptions.class).allowPlayerInventorySorting);
        assertTrue(NewConfigOptions.convertOldToNew(new net.kyrptonaught.inventorysorter.config.OldConfigOptions()).allowPlayerInventorySorting);
        var encoded = SortSettings.NBT_CODEC.encodeStart(JsonOps.INSTANCE, SortSettings.DEFAULT.withAllowPlayerInventorySorting(false)).getOrThrow();
        encoded.getAsJsonObject().remove("allowPlayerInventorySorting");
        assertTrue(SortSettings.NBT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().allowPlayerInventorySorting());
    }

    @Test
    void optOutPersistsAndSettingsUpdatesPreserveIt() {
        var settings = SortSettings.DEFAULT.withAllowPlayerInventorySorting(false);
        var encoded = SortSettings.NBT_CODEC.encodeStart(JsonOps.INSTANCE, settings).getOrThrow();
        assertEquals(settings, SortSettings.NBT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        List.of(settings.withDoubleClick(false), settings.withSortType(SortType.ID), settings.withSortPlayerInventory(true),
                settings.withSortHighlightedInventory(false), settings.withSortIntoBundles(false),
                settings.withSortIntoHotbarBundles(false), settings.withSortPriorityRules(List.of()))
                .forEach(updated -> assertFalse(updated.allowPlayerInventorySorting()));
        NewConfigOptions config = new NewConfigOptions();
        config.allowPlayerInventorySorting = false;
        assertFalse(SortSettings.fromConfig(config).allowPlayerInventorySorting());
    }

    @Test
    void legacyWireBytesRemainUnchangedAndPreferenceUsesSeparateCodec() {
        var enabled = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        var disabled = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            SortSettings.CODEC.encode(enabled, SortSettings.DEFAULT);
            SortSettings.CODEC.encode(disabled, SortSettings.DEFAULT.withAllowPlayerInventorySorting(false));
            assertEquals(enabled, disabled);
            assertTrue(SortSettings.CODEC.decode(disabled).allowPlayerInventorySorting());
            assertFalse(disabled.isReadable());
            assertEquals(PlayerInventorySortingPreference.ID, new PlayerInventorySortingPreference(false).type());
            assertNotEquals(SortSettings.ID, PlayerInventorySortingPreference.ID);
            PlayerInventorySortingPreference.CODEC.encode(disabled, new PlayerInventorySortingPreference(false));
            assertEquals(new PlayerInventorySortingPreference(false), PlayerInventorySortingPreference.CODEC.decode(disabled));
            assertFalse(disabled.isReadable());
        } finally {
            enabled.release();
            disabled.release();
        }
    }

    @Test
    void optOutBlocksOnlyPlayerTarget() {
        assertFalse(SortabilityPolicy.isTargetAllowed(SortTarget.PLAYER_INVENTORY, false));
        assertTrue(SortabilityPolicy.isTargetAllowed(SortTarget.CONTAINER, false));
        assertTrue(SortabilityPolicy.isTargetAllowed(SortTarget.PLAYER_INVENTORY, true));
    }
}
