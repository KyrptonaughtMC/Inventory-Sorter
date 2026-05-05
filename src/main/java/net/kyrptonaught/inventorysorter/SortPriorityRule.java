package net.kyrptonaught.inventorysorter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SortPriorityRule(String match, SortPriorityPosition position) {
    public static final Codec<SortPriorityRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("match").forGetter(SortPriorityRule::match),
            Codec.STRING.xmap(SortPriorityPosition::fromConfigValue, SortPriorityPosition::configValue)
                    .fieldOf("position").forGetter(SortPriorityRule::position)
    ).apply(instance, SortPriorityRule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SortPriorityRule> STREAM_CODEC =
            StreamCodec.ofMember(
                    (value, buf) -> {
                        buf.writeUtf(value.match());
                        buf.writeEnum(value.position());
                    },
                    buf -> new SortPriorityRule(buf.readUtf(), buf.readEnum(SortPriorityPosition.class))
            );

    public String configValue() {
        return match + "=" + position.configValue();
    }

    public static SortPriorityRule fromConfigValue(String value) {
        int separator = value.lastIndexOf('=');
        if (separator < 1 || separator == value.length() - 1) {
            throw new IllegalArgumentException("Expected '<match>=<first|default|last|ignore>'");
        }
        return new SortPriorityRule(
                value.substring(0, separator).trim(),
                SortPriorityPosition.fromConfigValue(value.substring(separator + 1))
        );
    }
}
