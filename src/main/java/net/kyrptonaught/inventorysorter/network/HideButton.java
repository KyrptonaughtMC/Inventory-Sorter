package net.kyrptonaught.inventorysorter.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashSet;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record HideButton(
        Set<String> hideButtonForScreens
) implements CustomPacketPayload {

    public static final Type<HideButton> ID = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sync_hide_button_packet"));
    public static final HideButton DEFAULT = new HideButton(Set.of());

    public static final StreamCodec<RegistryFriendlyByteBuf, HideButton> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeCollection(value.hideButtonForScreens(), FriendlyByteBuf::writeUtf);
                    },
                    buf -> new HideButton(
                            buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf)
                    )
            );

    public static HideButton fromConfig(CompatConfig config) {
        return new HideButton(new HashSet<>(config.hideButtonsForScreens));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void sync(ServerPlayer player) {
        ServerPlayNetworking.send(player, this);
    }

    public void sync(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::sync);
    }
}
