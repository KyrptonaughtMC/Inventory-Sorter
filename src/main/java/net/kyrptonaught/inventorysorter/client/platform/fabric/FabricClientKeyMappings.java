package net.kyrptonaught.inventorysorter.client.platform.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.client.platform.ClientKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class FabricClientKeyMappings implements ClientKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(InventorySorterMod.MOD_ID, "main"));

    private final InputConstants.Key modifierKey = InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LCONTROL);
    private final KeyMapping configKeyMapping = new KeyMapping(
            "inventorysorter.key.config",
            InputConstants.KEY_P,
            CATEGORY
    );
    private final KeyMapping sortKeyMapping = new KeyMapping(
            "inventorysorter.key.sort",
            InputConstants.KEY_P,
            CATEGORY
    );

    @Override
    public InputConstants.Key modifierKey() {
        return modifierKey;
    }

    @Override
    public KeyMapping configKeyMapping() {
        return configKeyMapping;
    }

    @Override
    public KeyMapping sortKeyMapping() {
        return sortKeyMapping;
    }

    @Override
    public InputConstants.Key boundConfigKey() {
        return KeyMappingHelper.getBoundKeyOf(configKeyMapping);
    }

    @Override
    public InputConstants.Key boundSortKey() {
        return KeyMappingHelper.getBoundKeyOf(sortKeyMapping);
    }

    @Override
    public void register() {
        KeyMappingHelper.registerKeyMapping(configKeyMapping);
        KeyMappingHelper.registerKeyMapping(sortKeyMapping);
    }
}
