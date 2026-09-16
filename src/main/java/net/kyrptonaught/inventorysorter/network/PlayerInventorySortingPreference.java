package net.kyrptonaught.inventorysorter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record PlayerInventorySortingPreference(boolean allowed) implements CustomPacketPayload {
    public static final Type<PlayerInventorySortingPreference> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "player_inventory_sorting_preference"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerInventorySortingPreference> CODEC = StreamCodec.ofMember(
            (value, buf) -> buf.writeBoolean(value.allowed()),
            buf -> new PlayerInventorySortingPreference(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
