package net.kyrptonaught.inventorysorter.client.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public interface ClientKeyMappings {
    InputConstants.Key modifierKey();

    KeyMapping configKeyMapping();

    KeyMapping sortKeyMapping();

    InputConstants.Key boundConfigKey();

    InputConstants.Key boundSortKey();

    void register();
}
