package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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

    public void sync(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(this::sync);
    }
}
