package net.kyrptonaught.inventorysorter.client;

import com.mojang.blaze3d.platform.InputConstants;
import gg.meza.supporters.clothconfig.SupportCategory;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.network.SortPriorityRuleSetting;
import net.kyrptonaught.inventorysorter.sort.SortType;
import net.kyrptonaught.inventorysorter.client.clothconfig.ContainerEntry;
import net.kyrptonaught.inventorysorter.client.clothconfig.SortPriorityRulesEntry;
import net.kyrptonaught.inventorysorter.client.platform.ClientPlatformServices;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.config.ScrollBehaviour;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.*;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

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

    public static void openIfConfigKeyPressed(Minecraft client) {
        if (consumeConfigScreenClick(
                ClientPlatformServices.KEY_MAPPINGS.configKeyMapping(),
                ClientPlatformServices.KEY_MAPPINGS.sortKeyMapping(),
                ClientPlatformServices.KEY_MAPPINGS.boundConfigKey(),
                ClientPlatformServices.KEY_MAPPINGS.boundSortKey()
        )) {
            client.setScreen(getConfigScreen(client.screen));
        }
    }

    static boolean consumeConfigScreenClick(KeyMapping configButton, KeyMapping sortButton, InputConstants.Key configKey, InputConstants.Key sortKey) {
        if (configKey.getValue() == sortKey.getValue()) {
            return sortButton.consumeClick() || configButton.consumeClick();
        }

        return configButton.consumeClick();
    }

    private static List<AbstractConfigListEntry<?>> buildCompatEditor(ConfigEntryBuilder builder, NewConfigOptions config) {
        Set<String> allScreens = new HashSet<>();
        allScreens.addAll(config.hideButtonsForScreens);
        allScreens.addAll(config.preventSortForScreens);
        List<AbstractConfigListEntry<?>> entries = new ArrayList<>();

        if (SortButtonDisplayPolicy.getLastCheckedId().isPresent()) {
            entries.add(builder.startTextDescription(Component.literal(" ")).build());
            String screenId = SortButtonDisplayPolicy.getLastCheckedId().get().toString();
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
            if (SortButtonDisplayPolicy.getLastCheckedId().isPresent() && SortButtonDisplayPolicy.getLastCheckedId().get().toString().equals(screenId)) {
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
        InputConstants.Key modifierKey = ClientPlatformServices.KEY_MAPPINGS.modifierKey();

        ConfigBuilder screenBuilder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(Identifier.fromNamespaceAndPath("minecraft", "textures/block/dirt.png"))
                .setTitle(Component.translatable("inventorysorter.config.screen.title"));
        ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

        screenBuilder.setSavingRunnable(() -> {
            getConfig().save();
            reloadConfig();
            if (Minecraft.getInstance().player != null)
                ClientConfigSync.syncConfigToServer();
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

        ConfigCategory logicCategory = screenBuilder.getOrCreateCategory(Component.translatable("inventorysorter.config.category.logic"));
        logicCategory.addEntry(entryBuilder.startEnumSelector(Component.translatable("inventorysorter.config.sortType"), SortType.class, options.sortType)
                        .setEnumNameProvider((sortType) -> Component.translatable(((SortType) sortType).getTranslationKey()))
                        .setDefaultValue(SortType.NAME)
                        .setSaveConsumer(val -> options.sortType = val)
                        .build());
        logicCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortPlayerInventory"), options.sortPlayerInventory)
                        .setDefaultValue(false)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortPlayerInventory.tooltip"))
                        .setSaveConsumer(val -> options.sortPlayerInventory = val)
                        .build());
        logicCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortHovered"), options.sortHighlightedItem)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortHovered.tooltip"))
                        .setSaveConsumer(val -> options.sortHighlightedItem = val)
                        .build());
        logicCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortIntoBundles"), options.sortIntoBundles)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortIntoBundles.tooltip"))
                        .setSaveConsumer(val -> options.sortIntoBundles = val)
                        .build());
        logicCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("inventorysorter.config.sortIntoHotbarBundles"), options.sortIntoHotbarBundles)
                        .setDefaultValue(true)
                        .setYesNoTextSupplier(ConfigScreen::toggleState)
                        .setTooltip(Component.translatable("inventorysorter.config.sortIntoHotbarBundles.tooltip"))
                        .setSaveConsumer(val -> options.sortIntoHotbarBundles = val)
                        .build());
        buildSortPriorityRulesEditor(entryBuilder, options).forEach(logicCategory::addEntry);

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

    private static List<AbstractConfigListEntry<?>> buildSortPriorityRulesEditor(ConfigEntryBuilder builder, NewConfigOptions options) {
        return List.of(
                builder.startTextDescription(Component.translatable("inventorysorter.config.sortPriorityRules.header")).build(),
                builder.startTextDescription(Component.translatable("inventorysorter.config.sortPriorityRules.description")).build(),
                new SortPriorityRulesEntry(
                builder,
                Component.translatable("inventorysorter.config.sortPriorityRules"),
                options.sortPriorityRules,
                rules -> options.sortPriorityRules = saveableSortPriorityRules(rules)
        ));
    }

    static List<SortPriorityRuleSetting> saveableSortPriorityRules(List<SortPriorityRuleSetting> rules) {
        return rules.stream()
                .filter(rule -> isNotBlank(rule.match()))
                .map(rule -> new SortPriorityRuleSetting(rule.match().trim(), rule.position()))
                .toList();
    }

    static Optional<Component> sortPriorityMatchError(String value) {
        if (!isNotBlank(value)) {
            return Optional.empty();
        }
        return SortPriorityRules.validationError(value)
                .map(message -> Component.translatable("inventorysorter.config.sortPriorityRules.error", message));
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

}
