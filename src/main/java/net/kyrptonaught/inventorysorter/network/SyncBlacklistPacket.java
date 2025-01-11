package net.kyrptonaught.inventorysorter.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.client.config.IgnoreList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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
        IgnoreList blacklist = InventorySorterMod.getBlackList();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        String[] hideList = new String[blacklist.hideSortBtnsList.size()];
        hideList = blacklist.hideSortBtnsList.toArray(hideList);
        buf.writeInt(hideList.length);
        for (String value : hideList) buf.writeString(value);

        String[] unSortList = new String[blacklist.doNotSortList.size()];
        unSortList = blacklist.doNotSortList.toArray(unSortList);
        buf.writeInt(unSortList.length);
        for (String s : unSortList) buf.writeString(s);
        return buf;
    }

    @Environment(EnvType.CLIENT)
    public static void registerReceiveBlackList() {
        ClientPlayNetworking.registerGlobalReceiver(SyncBlacklistPacket.ID, (payload, context) -> {
            PacketByteBuf packet = new PacketByteBuf(payload.buffer);
            int numHides = packet.readInt();
            for (int i = 0; i < numHides; i++)
                InventorySorterMod.getBlackList().hideSortBtnsList.add(packet.readString());

            int numNoSort = packet.readInt();
            for (int i = 0; i < numNoSort; i++)
                InventorySorterMod.getBlackList().doNotSortList.add(packet.readString());
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