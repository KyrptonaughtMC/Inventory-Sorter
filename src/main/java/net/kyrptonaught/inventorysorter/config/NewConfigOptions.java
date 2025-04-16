package net.kyrptonaught.inventorysorter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.client.util.InputUtil;

import java.io.*;
import java.nio.file.Path;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class NewConfigOptions extends CompatConfig {

    public boolean showSortButton = true;
    public boolean showTooltips = true;
    public boolean separateButton = true;
    public boolean sortPlayerInventory = false;
    public SortType sortType = SortType.NAME;
    public boolean enableDoubleClickSort = true;
    public boolean sortHighlightedItem = true;
    public ScrollBehaviour scrollBehaviour = ScrollBehaviour.FREE;

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

        try (Reader configReader = new FileReader(filePath.toFile())) {
            LOGGER.debug("Validating config file...");
            SchemaValidator.isValidJsonObject(configReader, SchemaValidator.CONFIG_SCHEMA, filePath.toString());
            LOGGER.debug("Config file is valid.");

            NewConfigOptions result = GSON.fromJson(new FileReader(filePath.toFile()), NewConfigOptions.class);

            if (result.customCompatibilityListDownloadUrl.startsWith("https://raw.githubusercontent.com/kyrptonaught")) {
                LOGGER.info("Old, redundant custom compatibility list URL detected. Fixing it!");
                result.customCompatibilityListDownloadUrl = "";
            }

            return result;
        } catch (FileNotFoundException e) {
            return new NewConfigOptions();
        } catch (Exception e) {
            LOGGER.error("There's an error in the config file inventorysorter.json:");
            throw new RuntimeException(e);
        }
    }

    public static NewConfigOptions convertOldToNew(OldConfigOptions oldOptions) {
        NewConfigOptions newOptions = new NewConfigOptions();
        newOptions.showSortButton = oldOptions.displaySort;
        newOptions.showTooltips = oldOptions.displayTooltip;
        newOptions.separateButton = oldOptions.seperateBtn;
        newOptions.sortPlayerInventory = oldOptions.sortPlayer;
        newOptions.sortType = oldOptions.sortType;
        newOptions.enableDoubleClickSort = oldOptions.doubleClickSort;
        newOptions.sortHighlightedItem = oldOptions.sortMouseHighlighted;

        if (oldOptions.keybinding != null) {
            // @TODO come up with something for this
            InputUtil.Key boundKey = InputUtil.fromTranslationKey(oldOptions.keybinding);
            InventorySorterModClient.sortButton.setBoundKey(boundKey); // this doesn't seem to take effect
        }

        return newOptions;
    }
}
