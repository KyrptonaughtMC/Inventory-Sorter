package net.kyrptonaught.inventorysorter.client.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.getConfig;

/**
 * Not using AutoConfig to maintain the translatable text
 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    private Text on() {
        return Text.translatable("key.inventorysorter.config.enabled").formatted(Formatting.GREEN);
    }

    private Text off() {
        return Text.translatable("key.inventorysorter.config.disabled").formatted(Formatting.RED);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {

            NewConfigOptions options = getConfig();

            ConfigBuilder screenBuilder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setDefaultBackgroundTexture(Identifier.of("minecraft", "textures/block/dirt.png"))
                    .setTitle(Text.translatable("key.inventorysorter.config"));
            ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

            screenBuilder.setSavingRunnable(() -> {
                getConfig().save();
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


            return screenBuilder.build();

        };
    }
}
