package net.kyrptonaught.inventorysorter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.minecraft.client.util.InputUtil;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;

public class NewConfigOptions extends CompatConfig {

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
    private static final String CLIENT_SCHEMA = "config-schema.json";

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
        URL url = NewConfigOptions.class.getClassLoader().getResource(CLIENT_SCHEMA);

        try(InputStream inputStream = url.openStream()) {
            LOGGER.debug("Validating config file...");
            JSONObject schema = new JSONObject(new JSONTokener(inputStream));
            Schema schemaValidator = SchemaLoader.load(schema);
            schemaValidator.validate(new JSONObject(new JSONTokener(new FileReader(filePath.toFile()))));
            LOGGER.debug("Config file is valid.");
        } catch (ValidationException e) {
            LOGGER.error("There's an error in the config file inventorysorter.json:");
            LOGGER.error(e.getErrorMessage());
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(LOGGER::error);
            throw new RuntimeException(e);
        }

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

        if (oldOptions.keybinding != null) {
            // @TODO come up with something for this
            InputUtil.Key boundKey = InputUtil.fromTranslationKey(oldOptions.keybinding);
            InventorySorterModClient.sortButton.setBoundKey(boundKey); // this doesn't seem to take effect
        }

        return newOptions;
    }
}
