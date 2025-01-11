package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.client.config.ConfigOptions;
import net.kyrptonaught.inventorysorter.interfaces.InvSorterPlayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SyncInvSortSettingsPacket(boolean middleClick, boolean doubleClick,
                                        int sortType) implements CustomPayload {
    private static final CustomPayload.Id<SyncInvSortSettingsPacket> ID = new CustomPayload.Id<>(Identifier.of("inventorysorter", "sync_settings_packet"));
    private static final PacketCodec<RegistryByteBuf, SyncInvSortSettingsPacket> CODEC = CustomPayload.codecOf(SyncInvSortSettingsPacket::write, SyncInvSortSettingsPacket::new);

    public SyncInvSortSettingsPacket(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    public static void registerSyncOnPlayerJoin() {
        ConfigOptions config = InventorySorterModClient.getConfig();
        ClientPlayNetworking.send(new SyncInvSortSettingsPacket(config.middleClick, config.doubleClickSort, config.sortType.ordinal()));
    }

    public static void registerReceiveSyncData() {
        PayloadTypeRegistry.playC2S().register(SyncInvSortSettingsPacket.ID, SyncInvSortSettingsPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SyncInvSortSettingsPacket.ID, ((payload, context) -> {
            ServerPlayerEntity player = context.player();
            player.getServer().execute(() -> {
                ((InvSorterPlayer) player).setMiddleClick(payload.middleClick);
                ((InvSorterPlayer) player).setDoubleClickSort(payload.doubleClick);
                ((InvSorterPlayer) player).setSortType(SortCases.SortType.values()[payload.sortType]);
            });
        }));
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(middleClick);
        buf.writeBoolean(doubleClick);
        buf.writeInt(sortType);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}