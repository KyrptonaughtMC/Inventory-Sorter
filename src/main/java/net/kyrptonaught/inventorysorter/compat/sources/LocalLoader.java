package net.kyrptonaught.inventorysorter.compat.sources;

import com.ibm.icu.impl.ClassLoaderUtil;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
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
        URL url = ClassLoaderUtil.getClassLoader().getResource(path);
        Set<Identifier> identifiers;

        try {
            FileReader fileInputStream = new FileReader(Paths.get(url.toURI()).toFile());
            identifiers = parseJson(fileInputStream);
        } catch (URISyntaxException | FileNotFoundException e) {
            LOGGER.info("Could not find file: " + path + " in jar, creating empty list");
            throw new RuntimeException(e);
        }
        return identifiers;
    }

}
