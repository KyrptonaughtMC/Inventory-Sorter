package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public record InventorySortPacket(boolean shouldSortPlayerInventory, int sortType) implements CustomPayload {
    private static final CustomPayload.Id<InventorySortPacket> ID = new CustomPayload.Id<>(Identifier.of("inventorysorter", "sort_inv_packet"));
    private static final PacketCodec<RegistryByteBuf, InventorySortPacket> CODEC = CustomPayload.codecOf(InventorySortPacket::write, InventorySortPacket::new);

    public InventorySortPacket(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    public static void registerReceivePacket() {
        PayloadTypeRegistry.playC2S().register(InventorySortPacket.ID, InventorySortPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(InventorySortPacket.ID, ((payload, context) -> {
            SortType sortType = SortType.values()[payload.sortType];
            ServerPlayerEntity player = context.player();
            player.getServer().execute(() -> InventoryHelper.sortInventory(player, payload.shouldSortPlayerInventory, sortType));
        }));
    }

    @Environment(EnvType.CLIENT)
    public static void sendSortPacket(boolean shouldSortPlayerInventory) {
        ClientPlayNetworking.send(new InventorySortPacket(shouldSortPlayerInventory, getConfig().sortType.ordinal()));
        if (!shouldSortPlayerInventory && getConfig().sortPlayerInventory)
            sendSortPacket(true);
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(shouldSortPlayerInventory);
        buf.writeInt(sortType);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
