package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record HideButton(
        Set<String> hideButtonForScreens
) implements CustomPayload {

    public static final Id<HideButton> ID = new Id<>(Identifier.of(MOD_ID, "sync_hide_button_packet"));
    public static final HideButton DEFAULT = new HideButton(Set.of());

    public static final PacketCodec<RegistryByteBuf, HideButton> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeCollection(value.hideButtonForScreens(), PacketByteBuf::writeString);
                    },
                    buf -> new HideButton(
                            buf.readCollection(HashSet::new, PacketByteBuf::readString)
                    )
            );

    public static final Codec<HideButton> NBT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.listOf()
                            .xmap(list -> (Set<String>) new HashSet<>(list), ArrayList::new)
                            .fieldOf("hideButtonForScreens")
                            .forGetter(HideButton::hideButtonForScreens)
            ).apply(instance, HideButton::new));

    public static HideButton fromConfig(CompatConfig config) {
        return new HideButton(new HashSet<>(config.hideButtonsForScreens));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, this);
    }
}
