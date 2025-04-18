package net.kyrptonaught.inventorysorter.compat;

import com.google.gson.Gson;
import net.kyrptonaught.inventorysorter.compat.sources.CompatibilityLoader;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class Compatibility {
    Set<Identifier> shouldHideSortButtons = ConcurrentHashMap.newKeySet();
    Set<Identifier> shouldPreventSort = ConcurrentHashMap.newKeySet();
    List<CompatibilityLoader> loaders = new ArrayList<>();

    public Compatibility(ArrayList<CompatibilityLoader> loaders) {
        this.loaders = loaders;
        this.load();
    }

    public void addLoader(CompatibilityLoader loader) {
        this.loaders.add(loader);
    }

    public void load() {

        for (CompatibilityLoader loader : loaders) {
            new Thread(() -> {
                try {
                    Set<Identifier> hideButtons = loader.getShouldHideSortButtons();
                    Set<Identifier> preventSort = loader.getPreventSort();

                    shouldHideSortButtons.addAll(hideButtons);
                    shouldPreventSort.addAll(preventSort);

                    LOGGER.debug("Successfully loaded compatibility data from {}",
                            loader.getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.error("Error loading compatibility data from {}",
                            loader.getClass().getSimpleName(), e);
                }
            }).start();
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

    public boolean isSortAllowed(Identifier inventoryIdentifier, Set<String> playerSortPrevention) {
        if (shouldPreventSort.contains(inventoryIdentifier)) {
            return false;
        }

        if (playerSortPrevention.contains(inventoryIdentifier.toString())) {
            return false;
        }

        return true;
    }

    public void addShouldHideSortButton(String identifier) {
        shouldHideSortButtons.add(Identifier.of(identifier));
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
