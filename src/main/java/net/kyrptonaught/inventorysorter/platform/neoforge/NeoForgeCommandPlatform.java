/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.platform.neoforge;

import net.kyrptonaught.inventorysorter.commands.CommandRegistry;
import net.kyrptonaught.inventorysorter.platform.CommandPlatform;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class NeoForgeCommandPlatform implements CommandPlatform {
    @Override
    public void registerCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandRegistry.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
*//*?}*/
