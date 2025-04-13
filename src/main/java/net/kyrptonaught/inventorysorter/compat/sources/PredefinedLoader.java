package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Set;

public class PredefinedLoader implements CompatibilityLoader{
    @Override
    public Set<Identifier> getPreventSort() {
        return Set.of(
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.CRAFTING)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.ANVIL)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.BEACON)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.BLAST_FURNACE)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.BREWING_STAND)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.CARTOGRAPHY_TABLE)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.CRAFTER_3X3)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.ENCHANTMENT)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.FURNACE)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.GRINDSTONE)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.LECTERN)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.LOOM)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.MERCHANT)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.SMOKER)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.STONECUTTER))
        );
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        return Set.of(
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.BEACON)),
                Objects.requireNonNull(Registries.SCREEN_HANDLER.getId(ScreenHandlerType.LOOM))
        );
    }
}
