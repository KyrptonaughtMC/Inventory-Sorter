package net.kyrptonaught.inventorysorter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyrptonaught.inventorysorter.SortCases;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class NewConfigOptions {

    public boolean showSortButton = true;
    public boolean showTooltips = true;
    public boolean separateButton = true;
    public boolean sortPlayerInventory = false;
    public SortCases.SortType sortType = SortCases.SortType.NAME;
    public boolean enableMiddleClickSort = true;
    public boolean enableDoubleClickSort = true;
    public boolean sortHighlightedItem = true;
    public boolean enableDebugMode = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = MOD_ID + ".json";

    public void save() {
        Path filePath = ConfigPathResolver.getConfigPath(CONFIG_FILE);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NewConfigOptions load() throws IOException {
        Path filePath = ConfigPathResolver.getConfigPath(CONFIG_FILE);
        return GSON.fromJson(new FileReader(filePath.toFile()), NewConfigOptions.class);
    }

    public static NewConfigOptions convertOldToNew(OldConfigOptions oldOptions) {
        NewConfigOptions newOptions = new NewConfigOptions();
        newOptions.showSortButton = oldOptions.displaySort;
        newOptions.showTooltips = oldOptions.displayTooltip;
        newOptions.separateButton = oldOptions.seperateBtn;
        newOptions.sortPlayerInventory = oldOptions.sortPlayer;
        newOptions.sortType = oldOptions.sortType;
        newOptions.enableMiddleClickSort = oldOptions.middleClick;
        newOptions.enableDoubleClickSort = oldOptions.doubleClickSort;
        newOptions.sortHighlightedItem = oldOptions.sortMouseHighlighted;
        newOptions.enableDebugMode = oldOptions.debugMode;
        return newOptions;
    }
}
