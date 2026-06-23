//? if fabric {
package net.kyrptonaught.inventorysorter.client.platform.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FabricClientKeyMappingsTest {
    @Test
    void exposesStableKeyMappingInstances() {
        FabricClientKeyMappings keyMappings = new FabricClientKeyMappings();

        Assertions.assertSame(keyMappings.configKeyMapping(), keyMappings.configKeyMapping());
        Assertions.assertSame(keyMappings.sortKeyMapping(), keyMappings.sortKeyMapping());
    }

    @Test
    void definesExpectedInventorySorterMappings() {
        FabricClientKeyMappings keyMappings = new FabricClientKeyMappings();

        Assertions.assertEquals("inventorysorter.key.config", keyMappings.configKeyMapping().getName());
        Assertions.assertEquals("inventorysorter.key.sort", keyMappings.sortKeyMapping().getName());
        Assertions.assertEquals(InputConstants.KEY_P, keyMappings.configKeyMapping().getDefaultKey().getValue());
        Assertions.assertEquals(InputConstants.KEY_P, keyMappings.sortKeyMapping().getDefaultKey().getValue());
    }

    @Test
    void exposesControlAsModifierKey() {
        FabricClientKeyMappings keyMappings = new FabricClientKeyMappings();

        Assertions.assertEquals(InputConstants.KEY_LCONTROL, keyMappings.modifierKey().getValue());
    }
}

//? }
