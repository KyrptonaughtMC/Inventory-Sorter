package net.kyrptonaught.inventorysorter.compat.sources;

import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigLoader implements CompatibilityLoader {
    private final Supplier<CompatConfig> config;
    private Set<Identifier> preventSort = new HashSet<>();
    private Set<Identifier> shouldHideSortButtons = new HashSet<>();

    public ConfigLoader(Supplier<CompatConfig> config) {
        this.config = config;
    }


    @Override
    public Set<Identifier> getPreventSort() {
        preventSort.clear();

        if (config.get().preventSortForScreens != null) {
            preventSort.addAll(config.get().preventSortForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));
        }

        return preventSort;
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        shouldHideSortButtons.clear();

        if (config.get().hideButtonsForScreens != null) {
            shouldHideSortButtons.addAll(config.get().hideButtonsForScreens.stream().map(Identifier::of).collect(Collectors.toSet()));
        }

        return shouldHideSortButtons;
    }
}
