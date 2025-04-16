package net.kyrptonaught.inventorysorter.client;

import gg.meza.supporters.clothconfig.SupportCategory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.config.ScrollBehaviour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.modifierButton;

public class ConfigScreen {

    private static Text on() {
        return Text.translatable("inventorysorter.toggle.enabled").formatted(Formatting.GREEN);
    }

    private static Text off() {
        return Text.translatable("inventorysorter.toggle.disabled").formatted(Formatting.RED);
    }

    private static Text yes() {
        return Text.translatable("inventorysorter.toggle.yes").formatted(Formatting.GREEN);
    }

    private static Text no() {
        return Text.translatable("inventorysorter.toggle.no").formatted(Formatting.RED);
    }

    private static Text toggleState(boolean state) {
        return state ? on() : off();
    }

    private static Text toggleYesNoState(boolean state) {
        return state ? yes() : no();
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
                    .startBooleanToggle(Text.translatable("inventorysorter.config.sortButton"), !shouldHide)
                    .setYesNoTextSupplier(ConfigScreen::toggleState)
                    .setTooltip(Text.translatable("inventorysorter.config.compat.hideButton.tooltip", screenId))
                    .setDefaultValue(true)
                    .setSaveConsumer(shouldShow -> {
                        if (shouldShow) {
                            config.enableButtonForScreen(screenId);
                        } else {
                            config.disableButtonForScreen(screenId);
                        }
                    })
                    .build();

            @NotNull BooleanListEntry preventToggle = builder
                    .startBooleanToggle(Text.translatable("inventorysorter.config.compat.preventSort"), shouldPreventSort)
                    .setYesNoTextSupplier(ConfigScreen::toggleYesNoState)
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
        InputUtil.Key modifierKey = KeyBindingHelper.getBoundKeyOf(modifierButton);

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
                .addEntry(entryBuilder.startEnumSelector(Text.translatable("inventorysorter.config.sortType"), SortType.class, options.sortType)
                        .setEnumNameProvider((sortType) -> Text.translatable(((SortType) sortType).getTranslationKey()))
                        .setDefaultValue(SortType.NAME)
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
                        .build())
                .addEntry(entryBuilder.startEnumSelector(Text.translatable("inventorysorter.config.scrollbehaviour"), ScrollBehaviour.class, options.scrollBehaviour)
                        .setEnumNameProvider((scrollBehaviour) -> Text.translatable(((ScrollBehaviour) scrollBehaviour).getTranslationKey()))
                        .setTooltipSupplier((scrollBehaviour) -> Optional.of(new MutableText[]{Text.translatable((scrollBehaviour).getTranslationKey()+".tooltip", modifierKey.getLocalizedText())}))
                        .setDefaultValue(ScrollBehaviour.FREE)
                        .setSaveConsumer(val -> options.scrollBehaviour = val)
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
                        .setExpanded(false)
                        .build()
        );


        List<SubCategoryListEntry> compatEntries = buildCompatEditor(entryBuilder, options);
        for (SubCategoryListEntry entry : compatEntries) {
            compatCategory.addEntry(entry);
        }

        SupportCategory.add(screenBuilder, entryBuilder);

        return screenBuilder.build();
    }
}
