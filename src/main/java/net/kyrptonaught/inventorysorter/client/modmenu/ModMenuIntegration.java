package net.kyrptonaught.inventorysorter.client.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.SortCases;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.kyrptonaught.inventorysorter.client.config.IgnoreList;
import net.kyrptonaught.inventorysorter.client.config.NewConfigOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Not using AutoConfig to maintain the translatable text
 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {

            NewConfigOptions options = InventorySorterModClient.getConfig();
            IgnoreList ignoreList = InventorySorterMod.getBlackList();

            ConfigBuilder screenBuilder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setDefaultBackgroundTexture(Identifier.of("minecraft", "textures/block/dirt.png"))
                    .setTitle(Text.translatable("key.inventorysorter.config"));
            ConfigEntryBuilder entryBuilder = screenBuilder.entryBuilder();

            screenBuilder.setSavingRunnable(() -> {
                InventorySorterModClient.getConfig().save();
                if (MinecraftClient.getInstance().player != null)
                    InventorySorterModClient.syncConfig();
            });

            screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.display"))
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.displaysort"), options.showSortButton)
                            .setDefaultValue(true)
                            .setTooltip(Text.translatable("key.inventorysorter.config.displaysort.tooltip"))
                            .setSaveConsumer(b -> options.showSortButton = b)
                            .build())
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.seperatebtn"), options.separateButton)
                            .setDefaultValue(true)
                            .setTooltip(Text.translatable("key.inventorysorter.config.seperatebtn.tooltip"))
                            .setSaveConsumer(b -> options.separateButton = b)
                            .build())
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.displaytooltip"), options.showTooltips)
                            .setDefaultValue(true)
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
                            .setTooltip(Text.translatable("key.inventorysorter.config.sortplayer.tooltip"))
                            .setSaveConsumer(val -> options.sortPlayerInventory = val)
                            .build());

            screenBuilder.getOrCreateCategory(Text.translatable("key.inventorysorter.config.category.activation"))
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.middleclick"), options.enableMiddleClickSort)
                            .setDefaultValue(true)
                            .setTooltip(Text.translatable("key.inventorysorter.config.middleclick.tooltip"))
                            .setSaveConsumer(val -> options.enableMiddleClickSort = val)
                            .build())
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.doubleclick"), options.enableDoubleClickSort)
                            .setDefaultValue(true)
                            .setTooltip(Text.translatable("key.inventorysorter.config.doubleclick.tooltip"))
                            .setSaveConsumer(val -> options.enableDoubleClickSort = val)
                            .build())
                    .addEntry(entryBuilder.startBooleanToggle(Text.translatable("key.inventorysorter.config.sortmousehighlighted"), options.sortHighlightedItem)
                            .setDefaultValue(true)
                            .setTooltip(Text.translatable("key.inventorysorter.config.sortmousehighlighted.tooltip"))
                            .setSaveConsumer(val -> options.sortHighlightedItem = val)
                            .build());


            return screenBuilder.build();

        };
    }
}
