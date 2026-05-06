package net.kyrptonaught.inventorysorter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.Optional;

/**
 * Canonical id used by compatibility config for menu-backed inventory screens.
 *
 * Minecraft exposes registered container identities through {@link AbstractContainerMenu#getType()}.
 * Player inventory screens do not have a useful registered menu id for this mod's compatibility
 * rules, so they keep the historical synthetic id used by existing configs.
 */
public record InventoryScreenId(Identifier value) {
    public static final InventoryScreenId PLAYER_INVENTORY = new InventoryScreenId(Identifier.parse("player_inventory"));

    public static Optional<InventoryScreenId> fromMenu(AbstractContainerMenu menu) {
        if (menu == null) {
            return Optional.empty();
        }

        if (menu instanceof InventoryMenu) {
            return Optional.of(PLAYER_INVENTORY);
        }

        try {
            MenuType<?> menuType = menu.getType();
            if (menuType == null) {
                return Optional.empty();
            }
            Identifier id = BuiltInRegistries.MENU.getKey(menuType);
            return id == null ? Optional.empty() : Optional.of(new InventoryScreenId(id));
        } catch (UnsupportedOperationException e) {
            return Optional.empty();
        }
    }

    public String serialized() {
        return value.toString();
    }

    @Override
    public String toString() {
        return serialized();
    }
}
