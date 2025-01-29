package net.kyrptonaught.inventorysorter.compat;

import com.google.gson.Gson;
import net.kyrptonaught.inventorysorter.compat.sources.CompatibilityLoader;
import net.minecraft.util.Identifier;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Compatibility {
    Set<Identifier> shouldHideSortButtons = new HashSet<>();
    Set<Identifier> shouldPreventSort = new HashSet<>();
    List<CompatibilityLoader> loaders = List.of();

    public Compatibility(List<CompatibilityLoader> loaders) {
        this.loaders = loaders;
        this.load();
    }

    public void load() {
        for (CompatibilityLoader loader : loaders) {
            shouldHideSortButtons.addAll(loader.getShouldHideSortButtons());
            shouldPreventSort.addAll(loader.getPreventSort());
        }
    }

    public void reload() {
        shouldHideSortButtons.clear();
        shouldPreventSort.clear();
        load();
    }

    public boolean shouldShowSortButton(Identifier inventoryIdentifier) {
        return !shouldHideSortButtons.contains(inventoryIdentifier);
    }

    public boolean canSort(Identifier inventoryIdentifier) {
        return !shouldPreventSort.contains(inventoryIdentifier);
    }

    public Set<Identifier> getShouldHideSortButtons() {
        return shouldHideSortButtons;
    }

    public Set<Identifier> getPreventSort() {
        return shouldPreventSort;
    }

    public boolean addShouldHideSortButton(String identifier) {
        return shouldHideSortButtons.add(Identifier.of(identifier));
    }

    public boolean addPreventSort(String identifier) {
        return shouldPreventSort.add(Identifier.of(identifier));
    }

    public static Set<Identifier> parseJson(Reader fileInputStream) {
        Set<Identifier> identifiers = new HashSet<>();
        Gson gson = new Gson().newBuilder().create();
        String[] rawIdentifiers = gson.fromJson(fileInputStream, String[].class);

        for (String rawIdentifier : rawIdentifiers) {
            Identifier identifier = Identifier.of(rawIdentifier);
            identifiers.add(identifier);
        }

        return identifiers;
    }
}
