package net.kyrptonaught.inventorysorter.compat.sources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.compat.Compatibility.parseJson;

public abstract class OnlineData implements CompatibilityLoader {

    public static class RemoteConfigData {
        public List<String> preventSortForScreens = new ArrayList<>();
        public List<String> hideButtonsForScreens = new ArrayList<>();

        public static RemoteConfigData downloadFrom(URL url) {
            try {
                Gson gson = new GsonBuilder().create();
                Reader reader = new InputStreamReader(url.openStream());

                return gson.fromJson(reader, RemoteConfigData.class);
            } catch (Exception e) {
                LOGGER.error("Error loading custom compatibility list from URL: {}", url);
            }
            return new RemoteConfigData();
        }
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
