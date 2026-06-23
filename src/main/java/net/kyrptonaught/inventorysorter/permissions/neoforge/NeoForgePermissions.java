/*? if neoforge {*/
/*package net.kyrptonaught.inventorysorter.permissions.neoforge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Arrays;
import java.util.function.Predicate;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public final class NeoForgePermissions {
    private NeoForgePermissions() {
    }

    public static Predicate<CommandSourceStack> require(String permission, int defaultRequiredLevel) {
        Permission.Atom atom = permission(permission);
        Permission.HasCommandLevel fallback = commandLevel(defaultRequiredLevel);

        return source -> source.permissions().hasPermission(atom) || source.permissions().hasPermission(fallback);
    }

    public static Predicate<CommandSourceStack> hasAny(String... permissions) {
        return source -> Arrays.stream(permissions).anyMatch(permission -> source.permissions().hasPermission(permission(permission)));
    }

    private static Permission.Atom permission(String permission) {
        return Permission.Atom.create(Identifier.fromNamespaceAndPath(MOD_ID, "command." + permission));
    }

    private static Permission.HasCommandLevel commandLevel(int defaultRequiredLevel) {
        return new Permission.HasCommandLevel(PermissionLevel.byId(defaultRequiredLevel));
    }
}
*//*?}*/
