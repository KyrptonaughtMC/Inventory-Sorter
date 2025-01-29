package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.util.Identifier;

import java.util.Set;

public class OfficialListLoader extends OnlineData {
    private static final String BASE_URL = "https://github.com/KyrptonaughtMC/Inventory-Sorter/refs/heads/main/src/main/resources/data/inventorysorter/";
    private static final String DO_NOT_SORT_FILE = BASE_URL + "do-not-sort.json";
    private static final String HIDE_SORT_BUTTONS_FILE = BASE_URL + "hide-sort-buttons.json";

    @Override
    public Set<Identifier> getPreventSort() {
        return downloadIndividualList(DO_NOT_SORT_FILE);
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        return downloadIndividualList(HIDE_SORT_BUTTONS_FILE);
    }
}
