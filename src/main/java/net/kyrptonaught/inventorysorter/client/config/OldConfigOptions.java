package net.kyrptonaught.inventorysorter.client.config;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import net.kyrptonaught.inventorysorter.SortCases;

import java.io.IOException;
import java.nio.file.Path;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class OldConfigOptions {

    public boolean displaySort = true;
    public boolean displayTooltip = true;
    public boolean seperateBtn = true;
    public boolean sortPlayer = false;
    public SortCases.SortType sortType = SortCases.SortType.NAME;
    public String keybinding;
    public boolean middleClick = true;
    public boolean doubleClickSort = true;
    public Boolean sortMouseHighlighted = true;
    public boolean debugMode = false;

    public static final String CONFIG_FILE = MOD_ID + "/config.json5";

    public void save() throws IOException {
        LOGGER.info("Saving config to " + CONFIG_FILE + "is deprecated, please use the new config system.");
    }

    public static OldConfigOptions load() throws SyntaxError, IOException {
        Path filePath = ConfigPathResolver.getConfigPath(CONFIG_FILE);
        Jankson jankson = Jankson.builder().build();
        JsonObject original = jankson.load(filePath.toFile());
        return jankson.fromJson(original, OldConfigOptions.class);
    }
}
