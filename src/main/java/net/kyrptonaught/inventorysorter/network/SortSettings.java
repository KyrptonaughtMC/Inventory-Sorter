package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record SortSettings(
        boolean sortHighlightedItem,
        boolean sortPlayerInventory,
        boolean enableDoubleClick,
        SortType sortType
) implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, SortSettings> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeBoolean(value.sortHighlightedItem());
                        buf.writeBoolean(value.sortPlayerInventory());
                        buf.writeBoolean(value.enableDoubleClick());
                        buf.writeEnumConstant(value.sortType());
                    },
                    buf -> new SortSettings(
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readEnumConstant(SortType.class)
                    )
            );

    public static final Codec<SortSettings> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("sortHighlightedItem").forGetter(SortSettings::sortHighlightedItem),
            Codec.BOOL.fieldOf("sortPlayerInventory").forGetter(SortSettings::sortPlayerInventory),
            Codec.BOOL.fieldOf("enableDoubleClick").forGetter(SortSettings::enableDoubleClick),
            Codec.STRING.xmap(SortType::valueOf, SortType::name)
                    .fieldOf("sortType").forGetter(SortSettings::sortType)
    ).apply(instance, SortSettings::new));

    public static final CustomPayload.Id<SortSettings> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "sync_settings_packet"));

    public static final SortSettings DEFAULT = new SortSettings(true, false, true, SortType.NAME);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public SortSettings withDoubleClick(boolean enabled) {
        return new SortSettings(this.sortHighlightedItem(), this.sortPlayerInventory(), enabled, this.sortType());
    }

    public SortSettings withSortType(SortType sortType) {
        return new SortSettings(this.sortHighlightedItem(), this.sortPlayerInventory(), this.enableDoubleClick(), sortType);
    }

    public SortSettings withSortPlayerInventory(boolean enabled) {
        return new SortSettings(this.sortHighlightedItem(), enabled, this.enableDoubleClick(), this.sortType());
    }

    public SortSettings withSortHighlightedInventory(boolean enabled) {
        return new SortSettings(enabled, this.sortPlayerInventory(), this.enableDoubleClick(), this.sortType());
    }

    public static SortSettings fromConfig(NewConfigOptions config) {
        return new SortSettings(
                config.sortHighlightedItem,
                config.sortPlayerInventory,
                config.enableDoubleClickSort,
                config.sortType
        );
    }

    public void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, this);
    }
}
