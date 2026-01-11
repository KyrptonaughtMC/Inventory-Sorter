package net.kyrptonaught.inventorysorter.compat.sources;

import java.util.Set;
import net.minecraft.resources.Identifier;

public interface CompatibilityLoader {
    Set<Identifier> getPreventSort();
    Set<Identifier> getShouldHideSortButtons();
}
