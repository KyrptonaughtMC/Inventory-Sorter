package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record SortSettings(
        boolean sortHighlightedItem,
        boolean sortPlayerInventory,
        boolean enableDoubleClick,
        SortType sortType,
        List<SortPriorityRule> sortPriorityRules
) implements CustomPacketPayload {
    public SortSettings(boolean sortHighlightedItem, boolean sortPlayerInventory, boolean enableDoubleClick, SortType sortType) {
        this(sortHighlightedItem, sortPlayerInventory, enableDoubleClick, sortType, List.of());
    }

    public SortSettings {
        sortPriorityRules = List.copyOf(sortPriorityRules);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SortSettings> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeBoolean(value.sortHighlightedItem());
                        buf.writeBoolean(value.sortPlayerInventory());
                        buf.writeBoolean(value.enableDoubleClick());
                        buf.writeEnum(value.sortType());
                        buf.writeVarInt(value.sortPriorityRules().size());
                        value.sortPriorityRules().forEach(rule -> SortPriorityRule.STREAM_CODEC.encode(buf, rule));
                    },
                    buf -> {
                        boolean sortHighlightedItem = buf.readBoolean();
                        boolean sortPlayerInventory = buf.readBoolean();
                        boolean enableDoubleClick = buf.readBoolean();
                        SortType sortType = buf.readEnum(SortType.class);
                        List<SortPriorityRule> sortPriorityRules = new java.util.ArrayList<>();
                        int rulesCount = buf.readVarInt();
                        for (int i = 0; i < rulesCount; i++) {
                            sortPriorityRules.add(SortPriorityRule.STREAM_CODEC.decode(buf));
                        }
                        return new SortSettings(
                                sortHighlightedItem,
                                sortPlayerInventory,
                                enableDoubleClick,
                                sortType,
                                sortPriorityRules
                        );
                    }
            );

    public static final Codec<SortSettings> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("sortHighlightedItem").forGetter(SortSettings::sortHighlightedItem),
            Codec.BOOL.fieldOf("sortPlayerInventory").forGetter(SortSettings::sortPlayerInventory),
            Codec.BOOL.fieldOf("enableDoubleClick").forGetter(SortSettings::enableDoubleClick),
            Codec.STRING.xmap(SortType::valueOf, SortType::name)
                    .fieldOf("sortType").forGetter(SortSettings::sortType),
            SortPriorityRule.CODEC.listOf()
                    .optionalFieldOf("sortPriorityRules", List.of())
                    .forGetter(SortSettings::sortPriorityRules)
    ).apply(instance, SortSettings::new));

    public static final CustomPacketPayload.Type<SortSettings> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_settings_packet"));

    public static final SortSettings DEFAULT = new SortSettings(true, false, true, SortType.NAME, List.of());

    public static SortSettings fromConfig(NewConfigOptions config) {
        return new SortSettings(
                config.sortHighlightedItem,
                config.sortPlayerInventory,
                config.enableDoubleClickSort,
                config.sortType,
                config.sortPriorityRules
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public SortSettings withDoubleClick(boolean enabled) {
        return new SortSettings(this.sortHighlightedItem(), this.sortPlayerInventory(), enabled, this.sortType(), this.sortPriorityRules());
    }

    public SortSettings withSortType(SortType sortType) {
        return new SortSettings(this.sortHighlightedItem(), this.sortPlayerInventory(), this.enableDoubleClick(), sortType, this.sortPriorityRules());
    }

    public SortSettings withSortPlayerInventory(boolean enabled) {
        return new SortSettings(this.sortHighlightedItem(), enabled, this.enableDoubleClick(), this.sortType(), this.sortPriorityRules());
    }

    public SortSettings withSortHighlightedInventory(boolean enabled) {
        return new SortSettings(enabled, this.sortPlayerInventory(), this.enableDoubleClick(), this.sortType(), this.sortPriorityRules());
    }

    public SortSettings withSortPriorityRules(List<SortPriorityRule> sortPriorityRules) {
        return new SortSettings(this.sortHighlightedItem(), this.sortPlayerInventory(), this.enableDoubleClick(), this.sortType(), sortPriorityRules);
    }

    public void sync(ServerPlayer player) {
        this.sync(player, PlatformServices.NETWORK);
    }

    void sync(ServerPlayer player, NetworkingPlatform networking) {
        networking.sendToPlayer(player, this);
    }
}
