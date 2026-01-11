package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public record InventorySortPacket(boolean shouldSortPlayerInventory, int sortType) implements CustomPacketPayload {
    private static final CustomPacketPayload.Type<InventorySortPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("inventorysorter", "sort_inv_packet"));
    private static final StreamCodec<RegistryFriendlyByteBuf, InventorySortPacket> CODEC = CustomPacketPayload.codec(InventorySortPacket::write, InventorySortPacket::new);

    public InventorySortPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    public static void registerReceivePacket() {
        PayloadTypeRegistry.playC2S().register(InventorySortPacket.ID, InventorySortPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(InventorySortPacket.ID, ((payload, context) -> {
            SortType sortType = SortType.values()[payload.sortType];
            ServerPlayer player = context.player();
            MinecraftServer server = player.level().getServer();
            server.execute(() -> InventoryHelper.sortInventory(player, payload.shouldSortPlayerInventory, sortType));
        }));
    }

    @Environment(EnvType.CLIENT)
    public static void sendSortPacket(boolean shouldSortPlayerInventory) {
        ClientPlayNetworking.send(new InventorySortPacket(shouldSortPlayerInventory, getConfig().sortType.ordinal()));
        if (!shouldSortPlayerInventory && getConfig().sortPlayerInventory)
            sendSortPacket(true);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(shouldSortPlayerInventory);
        buf.writeInt(sortType);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
