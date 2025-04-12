package net.kyrptonaught.inventorysorter.client;

import gg.meza.SupportersCore;

import gg.meza.supporters.Supporters;
import gg.meza.supporters.TierEntry;
import gg.meza.supporters.clothconfig.SupporterListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.*;
import java.util.List;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class ConfigScreen {

    private static final URI SPONSOR_URL = URI.create("https://ko-fi.com/meza");

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

    private static void renderSupporters(ConfigCategory supportCategory, ConfigEntryBuilder entryBuilder) {
        List<TierEntry> tiers = SupportersCore.getTiers();

        Text newSupportersText = SupportersCore.getNewSupportersText();
        if (!newSupportersText.getString().isBlank()) {
            supportCategory.addEntry(entryBuilder
                    .startTextDescription(Text.translatable(MOD_ID + ".config.support.newcomers"))
                    .build());
            supportCategory.addEntry(new SupporterListEntry(newSupportersText));
            supportCategory.addEntry(entryBuilder.startTextDescription(Text.literal(" ")).build());

        }
        if (!tiers.isEmpty()) {
            supportCategory.addEntry(entryBuilder.startTextDescription(Text.translatable("inventorysorter.config.support.list")).build());
        }
        for (TierEntry tier : tiers) {
            String title = tier.emoji != null ? tier.emoji + " " + tier.name : tier.name;

            SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Text.literal(title))
                    .setExpanded(true);

            subCategory.add(new SupporterListEntry(Supporters.asRainbowList(tier.members.stream().map(m -> m.name).toList())));
            supportCategory.addEntry(subCategory.build());
        }

        if (tiers.isEmpty()) {
            supportCategory.addEntry(entryBuilder.startTextDescription(Text.translatable(MOD_ID + ".config.support.empty")).build());
        }
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

        ConfigCategory supportCategory = screenBuilder.getOrCreateCategory(Text.literal("\uD83D\uDC99 ").append(Text.translatable(MOD_ID + ".config.category.support")))
                .addEntry(entryBuilder.startTextDescription(Text.translatable(MOD_ID + ".config.support.description1").append("\n").append(Text.translatable(MOD_ID + ".config.support.description2"))).build())

                .addEntry(new HeartTextEntry(Text.translatable(MOD_ID + ".config.support.cta")
                        .styled(style -> style
                                .withColor(Formatting.AQUA)
                                .withUnderline(true)
                                .withBold(true)
                                /*? if >=1.21.5 {*/
                                .withClickEvent(new ClickEvent.OpenUrl(SPONSOR_URL))
                                .withHoverEvent(new HoverEvent.ShowText(Text.translatable(MOD_ID + ".config.support.cta.tooltip")))
                                /*?} else {*/
                                /*.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SPONSOR_URL.toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(MOD_ID + ".config.support.cta.tooltip")))
                                *//*?}*/
                        )));

        renderSupporters(supportCategory, entryBuilder);

        return screenBuilder.build();
    }
}
