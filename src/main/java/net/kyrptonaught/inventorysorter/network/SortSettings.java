package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.SortCases;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static final Codec<SortSettings> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enableMiddleClick").forGetter(SortSettings::enableMiddleClick),
            Codec.BOOL.fieldOf("enableDoubleClick").forGetter(SortSettings::enableDoubleClick),
            Codec.STRING.xmap(SortCases.SortType::valueOf, SortCases.SortType::name)
                    .fieldOf("sortType").forGetter(SortSettings::sortType)
    ).apply(instance, SortSettings::new));

    public static final CustomPayload.Id<SortSettings> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "sync_settings_packet"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public SortSettings withMiddleClick(boolean enabled) {
        return new SortSettings(enabled, this.enableDoubleClick(), this.sortType());
    }

    public SortSettings withDoubleClick(boolean enabled) {
        return new SortSettings(this.enableMiddleClick(), enabled, this.sortType());
    }

    public SortSettings withSortType(SortCases.SortType sortType) {
        return new SortSettings(this.enableMiddleClick(), this.enableDoubleClick(), sortType);
    }

    public void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, this);
    }
}
