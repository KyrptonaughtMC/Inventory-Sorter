package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record LastSeenVersionPacket(
        String lastSeenVersion,
        String lastSeenLanguage
) implements CustomPayload {

    public static final Id<LastSeenVersionPacket> ID = new Id<>(Identifier.of(MOD_ID, "last_seen_version_packet"));
    public static final LastSeenVersionPacket DEFAULT = new LastSeenVersionPacket("", "");

    public static final PacketCodec<RegistryByteBuf, LastSeenVersionPacket> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeString(value.lastSeenVersion());
                        buf.writeString(value.lastSeenLanguage());
                    },
                    buf -> new LastSeenVersionPacket(buf.readString(), buf.readString())
            );

    public static final Codec<LastSeenVersionPacket> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("lastSeenVersion").forGetter(LastSeenVersionPacket::lastSeenVersion),
            Codec.STRING.fieldOf("lastSeenLanguage").forGetter(LastSeenVersionPacket::lastSeenLanguage)
    ).apply(instance, LastSeenVersionPacket::new));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, this);
    }
}
