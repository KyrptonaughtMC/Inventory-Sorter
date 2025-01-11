package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record InventorySortPacket(boolean playerInv, int sortType) implements CustomPayload {
    private static final CustomPayload.Id<InventorySortPacket> ID = new CustomPayload.Id<>(Identifier.of("inventorysorter", "sort_inv_packet"));
    private static final PacketCodec<RegistryByteBuf, InventorySortPacket> CODEC = CustomPayload.codecOf(InventorySortPacket::write, InventorySortPacket::new);

    public InventorySortPacket(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    public static void registerReceivePacket() {
        PayloadTypeRegistry.playC2S().register(InventorySortPacket.ID, InventorySortPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(InventorySortPacket.ID, ((payload, context) -> {
            SortCases.SortType sortType = SortCases.SortType.values()[payload.sortType];
            ServerPlayerEntity player = context.player();
            player.getServer().execute(() -> InventoryHelper.sortInv(player, payload.playerInv, sortType));
        }));
    }

    @Environment(EnvType.CLIENT)
    public static void sendSortPacket(boolean playerInv) {
        ClientPlayNetworking.send(new InventorySortPacket(playerInv, InventorySorterModClient.getConfig().sortType.ordinal()));
        if (!playerInv && InventorySorterModClient.getConfig().sortPlayer)
            sendSortPacket(true);
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(playerInv);
        buf.writeInt(sortType);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}