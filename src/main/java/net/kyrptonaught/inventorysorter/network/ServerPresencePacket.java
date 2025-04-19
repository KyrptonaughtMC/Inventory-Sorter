package net.kyrptonaught.inventorysorter.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record ServerPresencePacket() implements CustomPayload {

    public static final Id<ServerPresencePacket> ID = new Id<>(Identifier.of(MOD_ID, "client_sync_packet"));
    public static final ServerPresencePacket DEFAULT = new ServerPresencePacket();

    public static final PacketCodec<RegistryByteBuf, ServerPresencePacket> CODEC =
            PacketCodec.of(
                    (value, buf) -> {},
                    buf -> new ServerPresencePacket()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
