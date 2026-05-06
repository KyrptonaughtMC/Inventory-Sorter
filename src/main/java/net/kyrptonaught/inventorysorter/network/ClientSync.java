package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record ClientSync(
        boolean seenClient
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientSync> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "client_sync_packet"));
    public static final ClientSync DEFAULT = new ClientSync(false);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientSync> CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeBoolean(value.seenClient());
                    },
                    buf -> new ClientSync(buf.readBoolean())
            );

    public static final Codec<ClientSync> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("seenClient").forGetter(ClientSync::seenClient)
    ).apply(instance, ClientSync::new));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
