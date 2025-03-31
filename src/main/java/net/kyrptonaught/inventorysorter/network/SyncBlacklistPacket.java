package net.kyrptonaught.inventorysorter.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.compatibility;

public record SyncBlacklistPacket(ByteBuf buffer) implements CustomPayload {
    private static final CustomPayload.Id<SyncBlacklistPacket> ID = new CustomPayload.Id<>(Identifier.of("inventorysorter", "sync_blacklist_packet"));
    private static final PacketCodec<RegistryByteBuf, SyncBlacklistPacket> CODEC = CustomPayload.codecOf(SyncBlacklistPacket::write, SyncBlacklistPacket::new);

    public SyncBlacklistPacket(PacketByteBuf buf) {
        this(buf.readBytes(buf.readableBytes()));
    }

    public static void registerSyncOnPlayerJoin() {
        PayloadTypeRegistry.playS2C().register(SyncBlacklistPacket.ID, SyncBlacklistPacket.CODEC);
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> packetSender.sendPacket(new SyncBlacklistPacket(getBuf())));
    }

    public static void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncBlacklistPacket(getBuf()));
    }

    private static PacketByteBuf getBuf() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        Set<Identifier> shouldHideSortButtons = compatibility.getShouldHideSortButtons();
        buf.writeInt(shouldHideSortButtons.size());
        for (Identifier value : shouldHideSortButtons) buf.writeString(value.toString());

        Set<Identifier> preventSort = compatibility.getPreventSort();
        buf.writeInt(preventSort.size());
        for (Identifier value : preventSort) buf.writeString(value.toString());
        return buf;
    }

    @Environment(EnvType.CLIENT)
    public static void registerReceiveBlackList() {
        ClientPlayNetworking.registerGlobalReceiver(SyncBlacklistPacket.ID, (payload, context) -> {
            PacketByteBuf packet = new PacketByteBuf(payload.buffer);
            int numHides = packet.readInt();
            for (int i = 0; i < numHides; i++) {
                String readString = packet.readString();
                LOGGER.debug("Hiding sort button for: " + readString);
                compatibility.addShouldHideSortButton(readString);
            }

            int numNoSort = packet.readInt();
            for (int i = 0; i < numNoSort; i++) {
                String readString = packet.readString();
                LOGGER.debug("Preventing sort for: " + readString);
                compatibility.addPreventSort(readString);
            }
        });
    }

    public void write(PacketByteBuf buf) {
        buf.writeBytes(buffer);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
