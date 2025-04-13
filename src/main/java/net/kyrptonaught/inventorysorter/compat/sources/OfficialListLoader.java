package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.compat.Compatibility.parseJson;

public class OfficialListLoader implements CompatibilityLoader {
    private static final String BASE_URL = "https://github.com/KyrptonaughtMC/Inventory-Sorter/refs/heads/main/src/main/resources/data/inventorysorter/";
    private static final String DO_NOT_SORT_FILE = BASE_URL + "do-not-sort.json";
    private static final String HIDE_SORT_BUTTONS_FILE = BASE_URL + "hide-sort-buttons.json";

    public Set<Identifier> getPreventSort() {
        return downloadIndividualList(DO_NOT_SORT_FILE);
    }

    public Set<Identifier> getShouldHideSortButtons() {
        return downloadIndividualList(HIDE_SORT_BUTTONS_FILE);
    }

    protected Set<Identifier> downloadIndividualList(String URL) {
        LOGGER.debug("Loading compatibility data from: {}", URL);
        try {
            URL url = URI.create(URL).toURL();
            Reader reader = new InputStreamReader(url.openStream());
            return parseJson(reader);
        } catch (Exception e) {
            LOGGER.error("Error downloading compatibility data from URL: {}", URL);
        }
        return new HashSet<>();
    }
}
