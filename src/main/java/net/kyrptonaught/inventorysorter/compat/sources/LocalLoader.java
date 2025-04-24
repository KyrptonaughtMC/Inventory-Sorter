package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;
import static net.kyrptonaught.inventorysorter.compat.Compatibility.parseJson;

public class LocalLoader implements CompatibilityLoader {

    private static final String DO_NOT_SORT_DATA = "data/" + MOD_ID + "/do-not-sort.json";
    private static final String HIDE_BUTTONS_DATA = "data/" + MOD_ID + "/hide-buttons.json";

    public Set<Identifier> getPreventSort() {
        return load(DO_NOT_SORT_DATA);
    }

    public Set<Identifier> getShouldHideSortButtons() {
        return load(HIDE_BUTTONS_DATA);
    }

    private Set<Identifier> load(String path) {
        LOGGER.debug("Loading local compatibility data from: {}", path);
        Set<Identifier> identifiers = null;
        try (InputStream is = LocalLoader.class.getResourceAsStream(path)) {
            if (is != null) {
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                identifiers = parseJson(reader);
            }
        } catch (IOException e) {
            LOGGER.info("Could not find file: " + path + " in jar, creating empty list");
            throw new RuntimeException(e);
        }

        if (identifiers == null) {
            return Set.of();
        }

        return identifiers;
    }

}
