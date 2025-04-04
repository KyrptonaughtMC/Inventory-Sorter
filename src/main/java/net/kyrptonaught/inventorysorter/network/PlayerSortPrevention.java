package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record PlayerSortPrevention(
        Set<String> preventSortForScreens
) implements CustomPayload {

    public static final CustomPayload.Id<PlayerSortPrevention> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "sync_sort_prevention_packet"));
    public static final PlayerSortPrevention DEFAULT = new PlayerSortPrevention(Set.of());

    public static final PacketCodec<RegistryByteBuf, PlayerSortPrevention> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeCollection(value.preventSortForScreens(), PacketByteBuf::writeString);
                    },
                    buf -> new PlayerSortPrevention(
                            buf.readCollection(HashSet::new, PacketByteBuf::readString)
                    )
            );

    public static final Codec<PlayerSortPrevention> NBT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.listOf()
                            .xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new)
                            .fieldOf("preventSortForScreens")
                            .forGetter(PlayerSortPrevention::preventSortForScreens)
            ).apply(instance, PlayerSortPrevention::new));

    public static PlayerSortPrevention fromConfig(CompatConfig config) {
        return new PlayerSortPrevention(new HashSet<>(config.preventSortForScreens));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
