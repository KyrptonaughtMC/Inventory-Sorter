package net.kyrptonaught.inventorysorter.network;

import net.kyrptonaught.inventorysorter.SortCases;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record SortSettings(
        boolean enableMiddleClick,
        boolean enableDoubleClick,
        SortCases.SortType sortType
) implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, SortSettings> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeBoolean(value.enableMiddleClick());
                        buf.writeBoolean(value.enableDoubleClick());
                        buf.writeEnumConstant(value.sortType());
                    },
                    buf -> new SortSettings(
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readEnumConstant(SortCases.SortType.class)
                    )
            );

    public static final CustomPayload.Id<SortSettings> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "sync_settings_packet"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
