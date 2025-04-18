package net.kyrptonaught.inventorysorter.client.clothconfig;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.kyrptonaught.inventorysorter.client.ConfigScreen;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ContainerEntry {

    public static SubCategoryListEntry build(ConfigEntryBuilder builder, NewConfigOptions config, String screenId, boolean expanded) {
        @NotNull BooleanListEntry hideToggle = builder
                .startBooleanToggle(Text.translatable("inventorysorter.config.sortButton"), !config.hideButtonsForScreens.contains(screenId))
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
                .startBooleanToggle(Text.translatable("inventorysorter.config.compat.preventSort"), config.preventSortForScreens.contains(screenId))
                .setYesNoTextSupplier(ConfigScreen::toggleYesNoState)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("inventorysorter.config.compat.preventSort.tooltip", screenId))
                .setSaveConsumer(newValue -> {
                    if (newValue) config.disableSortForScreen(screenId);
                    else config.enableSortForScreen(screenId);
                })
                .build();

        MutableText subcategoryTitle = Text.literal(screenId);

        SubCategoryBuilder screenRow = builder.startSubCategory(subcategoryTitle).setExpanded(expanded);

        screenRow.add(hideToggle);
        screenRow.add(preventToggle);

        return screenRow.build();
    }
}
