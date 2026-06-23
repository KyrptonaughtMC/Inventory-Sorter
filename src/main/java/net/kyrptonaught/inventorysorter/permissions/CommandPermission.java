package net.kyrptonaught.inventorysorter.permissions;

//? if fabric {
import me.lucko.fabric.api.permissions.v0.Permissions;
import java.util.Arrays;
//? }

import net.minecraft.commands.CommandSourceStack;
//? if neoforge
//import net.kyrptonaught.inventorysorter.permissions.neoforge.NeoForgePermissions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class CommandPermission {

    private static @NotNull String getPermissionFor(@NotNull String permission) {
        return String.format("%s.command.%s", MOD_ID, permission);
    }

    public static @NotNull Predicate<CommandSourceStack> require(@NotNull String permission, int defaultRequiredLevel) {
        //? if fabric {
        return Permissions.require(getPermissionFor(permission), defaultRequiredLevel);
        //? }

        //? if neoforge {
        /*return NeoForgePermissions.require(permission, defaultRequiredLevel);
        *///?}
    }

    public static @NotNull Predicate<CommandSourceStack> hasAny(String... nodes) {
        //? if fabric {
        return source -> Arrays.stream(nodes).anyMatch(node -> Permissions.check(source, getPermissionFor(node)));
        //? }

        //? if neoforge {
        /*return NeoForgePermissions.hasAny(nodes);
        *///?}
    }

}
