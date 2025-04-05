package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public record ClientSync(
        boolean seenClient
) implements CustomPayload {

    public static final CustomPayload.Id<ClientSync> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "client_sync_packet"));
    public static final ClientSync DEFAULT = new ClientSync(false);

    public static final PacketCodec<RegistryByteBuf, ClientSync> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeBoolean(value.seenClient());
                    },
                    buf -> new ClientSync(buf.readBoolean())
            );

    public static final Codec<ClientSync> NBT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("seenClient").forGetter(ClientSync::seenClient)
    ).apply(instance, ClientSync::new));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
