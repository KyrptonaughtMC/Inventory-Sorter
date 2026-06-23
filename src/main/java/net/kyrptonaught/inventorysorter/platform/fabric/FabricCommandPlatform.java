//? if fabric {
package net.kyrptonaught.inventorysorter.platform.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.kyrptonaught.inventorysorter.commands.CommandRegistry;
import net.kyrptonaught.inventorysorter.platform.CommandPlatform;

public class FabricCommandPlatform implements CommandPlatform {
    @Override
    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);
    }
}
//? }
