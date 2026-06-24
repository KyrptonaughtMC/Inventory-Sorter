package net.kyrptonaught.inventorysorter.network;

import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record ReloadConfigPacket() implements CustomPacketPayload {

    public static final Type<ReloadConfigPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "client_reload_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadConfigPacket> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                    },
                    buf -> new ReloadConfigPacket()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void fire(ServerPlayer player) {
        this.fire(player, PlatformServices.NETWORK);
    }

    void fire(ServerPlayer player, NetworkingPlatform networking) {
        networking.sendToPlayer(player, this);
    }
}
