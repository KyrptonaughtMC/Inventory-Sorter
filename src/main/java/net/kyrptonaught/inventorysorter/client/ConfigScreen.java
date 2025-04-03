package net.kyrptonaught.inventorysorter.client;

import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class ConfigScreen {

    private static Text on() {
        return Text.translatable("key.inventorysorter.config.enabled").formatted(Formatting.GREEN);
    }

    private static Text off() {
        return Text.translatable("key.inventorysorter.config.disabled").formatted(Formatting.RED);
    }

    private static List<SubCategoryListEntry> buildCompatEditor(ConfigEntryBuilder builder, NewConfigOptions config) {
        Set<String> allScreens = new HashSet<>();
        allScreens.addAll(config.hideButtonsForScreens);
        allScreens.addAll(config.preventSortForScreens);
        List<SubCategoryListEntry> entries = new ArrayList<>();


        for (String screenId : allScreens) {
            boolean hide = config.hideButtonsForScreens.contains(screenId);
            boolean prevent = config.preventSortForScreens.contains(screenId);

            @NotNull BooleanListEntry hideToggle = builder
                    .startBooleanToggle(Text.literal("Hide Button"), hide)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> {
                        if (newValue) config.disableButtonForScreen(screenId);
                        else config.enableButtonForScreen(screenId);
                    })
                    .build();

            @NotNull BooleanListEntry preventToggle = builder
                    .startBooleanToggle(Text.literal("Prevent Sort"), prevent)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> {
                        if (newValue) config.disableSortForScreen(screenId);
                        else config.enableSortForScreen(screenId);
                    })
                    .build();

            SubCategoryBuilder screenRow = builder.startSubCategory(Text.literal(screenId)).setExpanded(false);
            screenRow.add(hideToggle);
            screenRow.add(preventToggle);

            entries.add(screenRow.build());
        }

        return entries;
    }



    public static Screen getConfigeScreen(Screen parent) {
        NewConfigOptions options = getConfig();

        ConfigBuilder screenBuilder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(Identifier.of("minecraft", "textures/block/dirt.png"))
                .setTitle(Text.translatable("key.inventorysorter.config"));
        ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

        screenBuilder.setSavingRunnable(() -> {
            getConfig().save();
            compatibility.reload();
            if (MinecraftClient.getInstance().player != null)
                InventorySorterModClient.syncConfig();
        });

        screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.display"))
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.displaysort"), options.showSortButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.displaysort.tooltip"))
                        .setSaveConsumer(b -> options.showSortButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.seperatebtn"), options.separateButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.seperatebtn.tooltip"))
                        .setSaveConsumer(b -> options.separateButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.displaytooltip"), options.showTooltips)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.displaytooltip.tooltip"))
                        .setSaveConsumer(b -> options.showTooltips = b)
                        .build());

        screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.logic"))
                .addEntry(entryBuilder.startEnumSelector(Text.translatable("key.inventorysorter.config.sorttype"), SortCases.SortType.class, options.sortType)
                        .setDefaultValue(SortCases.SortType.NAME)
                        .setTooltip(Text.translatable("key.inventorysorter.config.sorttype.tooltip"))
                        .setSaveConsumer(val -> options.sortType = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.sortplayer"), options.sortPlayerInventory)
                        .setDefaultValue(false)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.sortplayer.tooltip"))
                        .setSaveConsumer(val -> options.sortPlayerInventory = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.sortmousehighlighted"), options.sortHighlightedItem)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.sortmousehighlighted.tooltip"))
                        .setSaveConsumer(val -> options.sortHighlightedItem = val)
                        .build());

        screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.activation"))
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.middleclick"), options.enableMiddleClickSort)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.middleclick.tooltip"))
                        .setSaveConsumer(val -> options.enableMiddleClickSort = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.doubleclick"), options.enableDoubleClickSort)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(b -> b ? on() : off())
                        .setTooltip(Text.translatable("key.inventorysorter.config.doubleclick.tooltip"))
                        .setSaveConsumer(val -> options.enableDoubleClickSort = val)
                        .build());

        ConfigCategory compatCategory = screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.compat"));
        List<SubCategoryListEntry> compatEntries = buildCompatEditor(entryBuilder, options);
        for (SubCategoryListEntry entry : compatEntries) {
            compatCategory.addEntry(entry);
        }

        return screenBuilder.build();
    }
}
