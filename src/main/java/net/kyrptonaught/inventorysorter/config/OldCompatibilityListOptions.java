package net.kyrptonaught.inventorysorter.config;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class OldCompatibilityListOptions {

    public String blacklistDownloadURL;
    public List<String> doNotSortList = new ArrayList<>();
    public List<String> hideSortBtnsList = new ArrayList<>();

    public static final String CONFIG_FILE = MOD_ID + "/blacklist.json5";

    public static OldCompatibilityListOptions load() {
        try {
            Path filePath = ConfigPathResolver.getConfigPath(CONFIG_FILE);
            Jankson jankson = Jankson.builder().build();
            JsonObject original = jankson.load(filePath.toFile());
            return jankson.fromJson(original, OldCompatibilityListOptions.class);
        } catch (IOException | SyntaxError e) {
            return new OldCompatibilityListOptions();
        }
    }
}
