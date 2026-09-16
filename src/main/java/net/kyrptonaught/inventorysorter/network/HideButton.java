package net.kyrptonaught.inventorysorter.network;

import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.kyrptonaught.inventorysorter.platform.PlatformServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
            StreamCodec.composite(
                    ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
                    HideButton::hideButtonForScreens,
                    HideButton::new
            );

    public static HideButton fromConfig(CompatConfig config) {
        return new HideButton(new HashSet<>(config.hideButtonsForScreens));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void sync(ServerPlayer player) {
        this.sync(player, PlatformServices.NETWORK);
    }

    void sync(ServerPlayer player, NetworkingPlatform networking) {
        networking.sendToPlayer(player, this);
    }

    public void sync(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::sync);
    }
}
