package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record PlayerSortPrevention(
        Set<String> preventSortForScreens
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerSortPrevention> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_sort_prevention_packet"));
    public static final PlayerSortPrevention DEFAULT = new PlayerSortPrevention(Set.of());

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSortPrevention> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeCollection(value.preventSortForScreens(), FriendlyByteBuf::writeUtf);
                    },
                    buf -> new PlayerSortPrevention(
                            buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf)
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
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void sync(ServerPlayer player) {
        ServerPlayNetworking.send(player, this);
    }
}
