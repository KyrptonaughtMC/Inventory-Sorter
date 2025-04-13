package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record ReloadConfigPacket() implements CustomPayload {

    public static final Id<ReloadConfigPacket> ID = new Id<>(Identifier.of(MOD_ID, "client_reload_packet"));

    public static final PacketCodec<RegistryByteBuf, ReloadConfigPacket> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                    },
                    buf -> new ReloadConfigPacket()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void fire(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, this);
    }
}
