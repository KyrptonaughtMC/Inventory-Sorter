package net.kyrptonaught.inventorysorter.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.commands.CommandTranslations;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class ConfigScreen {

    private static Text on() {
        return Text.translatable("inventorysorter.toggle.enabled").formatted(Formatting.GREEN);
    }

    private static Text off() {
        return Text.translatable("inventorysorter.toggle.disabled").formatted(Formatting.RED);
    }

    private static Text toggleState(boolean state) {
        return state ? on() : off();
    }

    private static List<SubCategoryListEntry> buildCompatEditor(ConfigEntryBuilder builder, NewConfigOptions config) {
        Set<String> allScreens = new HashSet<>();
        allScreens.addAll(config.hideButtonsForScreens);
        allScreens.addAll(config.preventSortForScreens);
        List<SubCategoryListEntry> entries = new ArrayList<>();


        for (String screenId : allScreens) {
            boolean shouldHide = config.hideButtonsForScreens.contains(screenId);
            boolean shouldPreventSort = config.preventSortForScreens.contains(screenId);

            @NotNull BooleanListEntry hideToggle = builder
                    .startBooleanToggle(Text.translatable("inventorysorter.config.compat.hideButton"), shouldHide)
                    .setYesNoTextSupplier(ConfigScreen::toggleState)
                    .setTooltip(Text.translatable("inventorysorter.config.compat.hideButton.tooltip", screenId))
                    .setDefaultValue(false)
                    .setSaveConsumer(shouldNowHide -> {
                        if (shouldNowHide) {
                            config.disableButtonForScreen(screenId);
                        } else {
                            config.enableButtonForScreen(screenId);
                        }
                    })
                    .build();

            @NotNull BooleanListEntry preventToggle = builder
                    .startBooleanToggle(Text.translatable("inventorysorter.config.compat.preventSort"), shouldPreventSort)
                    .setYesNoTextSupplier(ConfigScreen::toggleState)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("inventorysorter.config.compat.preventSort.tooltip", screenId))
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
                .setTitle(Text.translatable("inventorysorter.config.screen.title"));
        ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

        screenBuilder.setSavingRunnable(() -> {
            getConfig().save();
            reloadConfig();
            if (MinecraftClient.getInstance().player != null)
                InventorySorterModClient.syncConfig();
        });

        screenBuilder.getOrCreateCategory(Text.translatable("inventorysorter.config.category.display"))
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.sortButton"), options.showSortButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.sortButton.tooltip"))
                        .setSaveConsumer(b -> options.showSortButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.separateButton"), options.separateButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.separateButton.tooltip"))
                        .setSaveConsumer(b -> options.separateButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.showTooltip"), options.showTooltips)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.showTooltip.tooltip"))
                        .setSaveConsumer(b -> options.showTooltips = b)
                        .build());

        screenBuilder.getOrCreateCategory(Text.translatable("inventorysorter.config.category.logic"))
                .addEntry(entryBuilder.startEnumSelector(Text.translatable("inventorysorter.config.sortType"), SortCases.SortType.class, options.sortType)
                        .setEnumNameProvider((sortType) -> Text.translatable(((SortCases.SortType) sortType).getTranslationKey()))
                        .setDefaultValue(SortCases.SortType.NAME)
                        .setSaveConsumer(val -> options.sortType = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.sortPlayerInventory"), options.sortPlayerInventory)
                        .setDefaultValue(false)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.sortPlayerInventory.tooltip"))
                        .setSaveConsumer(val -> options.sortPlayerInventory = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.sortHovered"), options.sortHighlightedItem)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.sortHovered.tooltip"))
                        .setSaveConsumer(val -> options.sortHighlightedItem = val)
                        .build());

        screenBuilder.getOrCreateCategory(Text.translatable("inventorysorter.config.category.activation"))
                .addEntry(entryBuilder.startBooleanToggle(Text.translatable("inventorysorter.config.doubleClickSort"), options.enableDoubleClickSort)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Text.translatable("inventorysorter.config.doubleClickSort.tooltip"))
                        .setSaveConsumer(val -> options.enableDoubleClickSort = val)
                        .build());

        ConfigCategory compatCategory = screenBuilder.getOrCreateCategory(Text.translatable("inventorysorter.config.category.compat"));

        FullWidthStringListEntry stringListEntry = new FullWidthStringListEntry(
                Text.translatable("inventorysorter.config.compat.remoteUrl"),
                options.customCompatibilityListDownloadUrl,
                Text.translatable("inventorysorter.config.compat.remoteUrl.reset"),
                () -> "",
                () -> Optional.of(new MutableText[]{Text.translatable("inventorysorter.config.compat.remoteUrl.tooltip")}),
                false
        );

        compatCategory.addEntry(
                entryBuilder.startSubCategory(
                                Text.translatable("inventorysorter.config.compat.remoteUrl"), List.of(stringListEntry))
                        .setExpanded(true)
                        .build()
        );


        List<SubCategoryListEntry> compatEntries = buildCompatEditor(entryBuilder, options);
        for (SubCategoryListEntry entry : compatEntries) {
            compatCategory.addEntry(entry);
        }

        return screenBuilder.build();
    }
}
