package net.kyrptonaught.inventorysorter.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SortPriorityRuleSetting(String match, SortPriorityPosition position) {
    public static final Codec<SortPriorityRuleSetting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("match").forGetter(SortPriorityRuleSetting::match),
            Codec.STRING.xmap(SortPriorityPosition::fromConfigValue, SortPriorityPosition::configValue)
                    .fieldOf("position").forGetter(SortPriorityRuleSetting::position)
    ).apply(instance, SortPriorityRuleSetting::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SortPriorityRuleSetting> STREAM_CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeUtf(value.match());
                        buf.writeEnum(value.position());
                    },
                    buf -> new SortPriorityRuleSetting(buf.readUtf(), buf.readEnum(SortPriorityPosition.class))
            );
}
