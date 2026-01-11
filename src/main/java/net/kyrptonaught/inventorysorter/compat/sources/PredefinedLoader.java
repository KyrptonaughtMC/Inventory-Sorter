package net.kyrptonaught.inventorysorter.compat.sources;

import java.util.Objects;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class PredefinedLoader implements CompatibilityLoader{
    @Override
    public Set<Identifier> getPreventSort() {
        return Set.of(
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.CRAFTING)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.ANVIL)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.BEACON)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.BLAST_FURNACE)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.BREWING_STAND)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.CARTOGRAPHY_TABLE)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.CRAFTER_3x3)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.ENCHANTMENT)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.FURNACE)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.GRINDSTONE)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.LECTERN)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.LOOM)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.MERCHANT)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.SMOKER)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.STONECUTTER))
        );
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        return Set.of(
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.BEACON)),
                Objects.requireNonNull(BuiltInRegistries.MENU.getKey(MenuType.LOOM))
        );
    }
}
