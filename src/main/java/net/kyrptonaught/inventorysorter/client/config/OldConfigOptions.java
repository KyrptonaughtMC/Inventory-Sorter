package net.kyrptonaught.inventorysorter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import net.kyrptonaught.inventorysorter.SortCases;
import net.minecraft.client.util.InputUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;
import static net.kyrptonaught.inventorysorter.InventorySorterMod.MOD_ID;
import static net.minecraft.client.util.InputUtil.GLFW_KEY_P;

public class OldConfigOptions {

    public boolean displaySort = true;
    public boolean displayTooltip = true;
    public boolean seperateBtn = true;
    public boolean sortPlayer = false;
    public SortCases.SortType sortType = SortCases.SortType.NAME;
    public InputUtil.Key keybinding = InputUtil.fromKeyCode(GLFW_KEY_P, 0);
    public boolean middleClick = true;
    public boolean doubleClickSort = true;
    public Boolean sortMouseHighlighted = true;
    public boolean debugMode = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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

    public static String stripComments(String json) {
        return json.replaceAll("(?s)/\\*.*?\\*/|//.*", "");
    }

    public static String readFile(Path filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
