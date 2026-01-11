package net.kyrptonaught.inventorysorter.client;

import gg.meza.supporters.clothconfig.SupportCategory;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.InventoryHelper;
import net.kyrptonaught.inventorysorter.SortType;
import net.kyrptonaught.inventorysorter.client.clothconfig.ContainerEntry;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.config.ScrollBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import java.util.*;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;
import static net.kyrptonaught.inventorysorter.client.InventorySorterModClient.modifierButton;

import com.mojang.blaze3d.platform.InputConstants;

public class ConfigScreen {

    private static Component on() {
        return Component.translatable("inventorysorter.toggle.enabled").withStyle(ChatFormatting.GREEN);
    }

    private static Component off() {
        return Component.translatable("inventorysorter.toggle.disabled").withStyle(ChatFormatting.RED);
    }

    private static Component yes() {
        return Component.translatable("inventorysorter.toggle.yes").withStyle(ChatFormatting.GREEN);
    }

    private static Component no() {
        return Component.translatable("inventorysorter.toggle.no").withStyle(ChatFormatting.RED);
    }

    public static Component toggleState(boolean state) {
        return state ? on() : off();
    }

    public static Component toggleYesNoState(boolean state) {
        return state ? yes() : no();
    }

    private static List<AbstractConfigListEntry<?>> buildCompatEditor(ConfigEntryBuilder builder, NewConfigOptions config) {
        Set<String> allScreens = new HashSet<>();
        allScreens.addAll(config.hideButtonsForScreens);
        allScreens.addAll(config.preventSortForScreens);
        List<AbstractConfigListEntry<?>> entries = new ArrayList<>();

        if (InventoryHelper.getLastCheckedId().isPresent()) {
            entries.add(builder.startTextDescription(Component.literal(" ")).build());
            String screenId = InventoryHelper.getLastCheckedId().get().toString();
            SubCategoryListEntry lastOpenedRow = ContainerEntry.build(builder, config, screenId, true);
            SubCategoryBuilder lastOpened = builder.startSubCategory(Component.translatable("inventorysorter.config.compat.lastOpened"))
                    .setExpanded(true);
            lastOpened.add(lastOpenedRow);
            entries.add(lastOpened.build());
            entries.add(builder.startTextDescription(Component.literal(" ")).build());
        }

        SubCategoryBuilder otherScreens = builder.startSubCategory(Component.translatable("inventorysorter.config.compat.others"))
                .setExpanded(false);
        for (String screenId : allScreens) {
            if (InventoryHelper.getLastCheckedId().isPresent() && InventoryHelper.getLastCheckedId().get().toString().equals(screenId)) {
                continue;
            }
            SubCategoryListEntry screenRow = ContainerEntry.build(builder, config, screenId, false);
            otherScreens.add(screenRow);
        }

        entries.add(otherScreens.build());

        return entries;
    }

    public static Screen getConfigScreen(Screen parent) {
        NewConfigOptions options = getConfig();
        InputConstants.Key modifierKey = modifierButton;

        ConfigBuilder screenBuilder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(Identifier.fromNamespaceAndPath("minecraft", "textures/block/dirt.png"))
                .setTitle(Component.translatable("inventorysorter.config.screen.title"));
        ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

        screenBuilder.setSavingRunnable(() -> {
            getConfig().save();
            reloadConfig();
            if (Minecraft.getInstance().player != null)
                InventorySorterModClient.syncConfig();
        });

        screenBuilder.getOrCreateCategory(Component.translatable("inventorysorter.config.category.display"))
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortButton"), options.showSortButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortButton.tooltip"))
                        .setSaveConsumer(b -> options.showSortButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.separateButton"), options.separateButton)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.separateButton.tooltip"))
                        .setSaveConsumer(b -> options.separateButton = b)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.showTooltip"), options.showTooltips)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.showTooltip.tooltip"))
                        .setSaveConsumer(b -> options.showTooltips = b)
                        .build());

        screenBuilder.getOrCreateCategory(Component.translatable("inventorysorter.config.category.logic"))
                .addEntry(entryBuilder.startEnumSelector(Component.translatable("inventorysorter.config.sortType"), SortType.class, options.sortType)
                        .setEnumNameProvider((sortType) -> Component.translatable(((SortType) sortType).getTranslationKey()))
                        .setDefaultValue(SortType.NAME)
                        .setSaveConsumer(val -> options.sortType = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortPlayerInventory"), options.sortPlayerInventory)
                        .setDefaultValue(false)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortPlayerInventory.tooltip"))
                        .setSaveConsumer(val -> options.sortPlayerInventory = val)
                        .build())
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortHovered"), options.sortHighlightedItem)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortHovered.tooltip"))
                        .setSaveConsumer(val -> options.sortHighlightedItem = val)
                        .build());

        screenBuilder.getOrCreateCategory(Component.translatable("inventorysorter.config.category.activation"))
                .addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.doubleClickSort"), options.enableDoubleClickSort)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.doubleClickSort.tooltip"))
                        .setSaveConsumer(val -> options.enableDoubleClickSort = val)
                        .build())
                .addEntry(entryBuilder.startEnumSelector(Component.translatable("inventorysorter.config.scrollbehaviour"), ScrollBehaviour.class, options.scrollBehaviour)
                        .setEnumNameProvider((scrollBehaviour) -> Component.translatable(((ScrollBehaviour) scrollBehaviour).getTranslationKey()))
                        .setTooltipSupplier((scrollBehaviour) -> Optional.of(new MutableComponent[]{Component.translatable((scrollBehaviour).getTranslationKey() + ".tooltip", modifierKey.getDisplayName())}))
                        .setDefaultValue(ScrollBehaviour.FREE)
                        .setSaveConsumer(val -> options.scrollBehaviour = val)
                        .build());

        ConfigCategory compatCategory = screenBuilder.getOrCreateCategory(Component.translatable("inventorysorter.config.category.compat"));

        FullWidthStringListEntry stringListEntry = new FullWidthStringListEntry(
                Component.translatable("inventorysorter.config.compat.remoteUrl"),
                options.customCompatibilityListDownloadUrl,
                Component.translatable("inventorysorter.config.compat.remoteUrl.reset"),
                () -> "",
                () -> Optional.of(new MutableComponent[]{Component.translatable("inventorysorter.config.compat.remoteUrl.tooltip")}),
                false
        );

        compatCategory.addEntry(
                entryBuilder.startSubCategory(
                                Component.translatable("inventorysorter.config.compat.remoteUrl"), List.of(stringListEntry))
                        .setExpanded(false)
                        .build()
        );


        List<AbstractConfigListEntry<?>> compatEntries = buildCompatEditor(entryBuilder, options);
        for (AbstractConfigListEntry<?> entry : compatEntries) {
            compatCategory.addEntry(entry);
        }

        try {
            SupportCategory.add(screenBuilder, entryBuilder);
        } catch (Exception e) {
            LOGGER.debug("Failed to add Supporter category", e);
        }

        return screenBuilder.build();
    }
}
