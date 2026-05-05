package net.kyrptonaught.inventorysorter.network;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.kyrptonaught.inventorysorter.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.kyrptonaught.inventorysorter.SortTarget;
import net.kyrptonaught.inventorysorter.SortType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class PacketCodecTest {

    @Test
    public void clientSyncCodecsRoundTripSeenState() {
        assertStreamRoundTrip(ClientSync.CODEC, new ClientSync(true));
        assertStreamRoundTrip(ClientSync.CODEC, ClientSync.DEFAULT);
        assertDataCodecRoundTrip(ClientSync.NBT_CODEC, new ClientSync(true));
    }

    @Test
    public void sortSettingsCodecsRoundTripAllSettings() {
        SortSettings settings = new SortSettings(
                false,
                true,
                false,
                SortType.CATEGORY,
                List.of(
                        new SortPriorityRule("#minecraft:shulker_boxes", SortPriorityPosition.FIRST),
                        new SortPriorityRule("@minecraft:bundle_contents", SortPriorityPosition.LAST)
                )
        );

        assertStreamRoundTrip(SortSettings.CODEC, settings);
        assertDataCodecRoundTrip(SortSettings.NBT_CODEC, settings);
    }

    @Test
    public void sortSettingsNbtCodecReadsMissingPriorityRulesAsEmptyForExistingPlayerData() {
        JsonElement encoded = SortSettings.NBT_CODEC.encodeStart(
                JsonOps.INSTANCE,
                new SortSettings(false, true, false, SortType.CATEGORY)
        ).getOrThrow();
        encoded.getAsJsonObject().remove("sortPriorityRules");

        Assertions.assertEquals(
                new SortSettings(false, true, false, SortType.CATEGORY),
                SortSettings.NBT_CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow()
        );
    }

    @Test
    public void playerSortPreventionCodecsRoundTripScreensAsASet() {
        PlayerSortPrevention prevention = new PlayerSortPrevention(Set.of(
                "minecraft:generic_9x3",
                "inventorysorter:test_screen"
        ));

        assertStreamRoundTrip(PlayerSortPrevention.CODEC, prevention);
        assertStreamRoundTrip(PlayerSortPrevention.CODEC, PlayerSortPrevention.DEFAULT);
        assertDataCodecRoundTrip(PlayerSortPrevention.NBT_CODEC, prevention);
    }

    @Test
    public void hideButtonCodecRoundTripsScreensAsASet() {
        assertStreamRoundTrip(HideButton.CODEC, new HideButton(Set.of(
                "minecraft:anvil",
                "inventorysorter:custom_screen"
        )));
        assertStreamRoundTrip(HideButton.CODEC, HideButton.DEFAULT);
    }

    @Test
    public void lastSeenVersionCodecsRoundTripVersionAndLanguage() {
        LastSeenVersionPacket packet = new LastSeenVersionPacket("1.21.6", "en_us");

        assertStreamRoundTrip(LastSeenVersionPacket.CODEC, packet);
        assertStreamRoundTrip(LastSeenVersionPacket.CODEC, LastSeenVersionPacket.DEFAULT);
        assertDataCodecRoundTrip(LastSeenVersionPacket.NBT_CODEC, packet);
    }

    @Test
    public void emptySignalPacketCodecsRoundTrip() {
        assertStreamRoundTrip(ReloadConfigPacket.CODEC, new ReloadConfigPacket());
        assertStreamRoundTrip(ServerPresencePacket.CODEC, ServerPresencePacket.DEFAULT);
    }

    @Test
    public void inventorySortPacketCodecRoundTripsSortRequest() {
        assertStreamRoundTrip(InventorySortPacket.CODEC, new InventorySortPacket(SortTarget.CONTAINER, SortType.CATEGORY));
        assertStreamRoundTrip(InventorySortPacket.CODEC, new InventorySortPacket(SortTarget.PLAYER_INVENTORY, SortType.NAME));
    }

    @Test
    public void inventorySortPacketCodecRejectsUnknownSortTypeOrdinal() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        buf.writeBoolean(true);
        buf.writeVarInt(999);

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> InventorySortPacket.CODEC.decode(buf));
    }

    private static <T> void assertStreamRoundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
        Assertions.assertEquals(value, streamRoundTrip(codec, value));
    }

    private static <T> T streamRoundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        codec.encode(buf, value);

        return codec.decode(buf);
    }

    private static <T> void assertDataCodecRoundTrip(Codec<T> codec, T value) {
        JsonElement encoded = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();

        Assertions.assertEquals(value, codec.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }
}
