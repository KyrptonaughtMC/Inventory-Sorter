package net.kyrptonaught.inventorysorter.network;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerPresencePacket() implements CustomPacketPayload {

    public static final Type<ServerPresencePacket> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "server_presence_packet"));
    public static final ServerPresencePacket DEFAULT = new ServerPresencePacket();

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerPresencePacket> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {},
                    buf -> new ServerPresencePacket()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
