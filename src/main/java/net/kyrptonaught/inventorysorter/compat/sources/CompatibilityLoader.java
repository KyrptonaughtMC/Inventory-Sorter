package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.resources.Identifier;

import java.util.Set;

public interface CompatibilityLoader {
    Set<Identifier> getPreventSort();

    Set<Identifier> getShouldHideSortButtons();
}
