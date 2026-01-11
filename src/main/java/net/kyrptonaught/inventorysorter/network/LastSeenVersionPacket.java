package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record LastSeenVersionPacket(
        String lastSeenVersion,
        String lastSeenLanguage
) implements CustomPacketPayload {

    public static final Type<LastSeenVersionPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "last_seen_version_packet"));
    public static final LastSeenVersionPacket DEFAULT = new LastSeenVersionPacket("", "");

    public static final StreamCodec<RegistryFriendlyByteBuf, LastSeenVersionPacket> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeUtf(value.lastSeenVersion());
                        buf.writeUtf(value.lastSeenLanguage());
                    },
                    buf -> new LastSeenVersionPacket(buf.readUtf(), buf.readUtf())
            );

    public static final Codec<LastSeenVersionPacket> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("lastSeenVersion").forGetter(LastSeenVersionPacket::lastSeenVersion),
            Codec.STRING.fieldOf("lastSeenLanguage").forGetter(LastSeenVersionPacket::lastSeenLanguage)
    ).apply(instance, LastSeenVersionPacket::new));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void send(ServerPlayer player) {
        ServerPlayNetworking.send(player, this);
    }
}
