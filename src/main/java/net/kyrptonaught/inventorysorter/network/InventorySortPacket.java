package net.kyrptonaught.inventorysorter.network;

import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

public record InventorySortPacket(boolean shouldSortPlayerInventory, SortType sortType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InventorySortPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("inventorysorter", "sort_inv_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventorySortPacket> CODEC = CustomPacketPayload.codec(InventorySortPacket::write, InventorySortPacket::new);

    public InventorySortPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readEnum(SortType.class));
    }

    public static void sendSortPacket(boolean shouldSortPlayerInventory) {
        sendSortPacket(shouldSortPlayerInventory, getConfig(), PlatformServices.NETWORK);
    }

    static void sendSortPacket(boolean shouldSortPlayerInventory, NewConfigOptions config, NetworkingPlatform networking) {
        networking.sendToServer(new InventorySortPacket(shouldSortPlayerInventory, config.sortType));
        if (!shouldSortPlayerInventory && config.sortPlayerInventory) {
            sendSortPacket(true, config, networking);
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(shouldSortPlayerInventory);
        buf.writeEnum(sortType);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
