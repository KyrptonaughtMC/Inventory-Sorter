package net.kyrptonaught.inventorysorter.permissions;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class CommandPermission {

    private static @NotNull String getPermissionFor(@NotNull String permission) {
        return String.format("%s.command.%s", MOD_ID, permission);
    }

    public static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequiredLevel) {
        return Permissions.require(getPermissionFor(permission), defaultRequiredLevel);
    }

    public static @NotNull Predicate<ServerCommandSource> hasAny(String... nodes) {
        return source -> Arrays.stream(nodes).anyMatch(node -> Permissions.check(source, getPermissionFor(node)));
    }

}
