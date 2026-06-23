package net.kyrptonaught.inventorysorter.client;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//? }
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/*
  "in memory of layout systems that never learned to stretch properly" - chatgpt's reaction to this file

  All of this is a copy of the original TextFieldEntry class, but with the width set to the full width of the screen.
  That's it...
 */
//? if fabric
@Environment(EnvType.CLIENT)
public abstract class FullWidthTextFieldEntry<T> extends TooltipListEntry<T> {
    private static final int ERROR_TEXT_COLOR = ARGB.opaque(0xFF5555);
    private static final int TEXT_COLOR = ARGB.opaque(0xE0E0E0);
    private static final int TEXT_FIELD_PADDING = 6;

    protected EditBox textFieldWidget;
    protected Button resetButton;
    protected Supplier<T> defaultValue;
    protected T original;
    protected List<AbstractWidget> widgets;
    private boolean isSelected;

    protected FullWidthTextFieldEntry(Component fieldName, T original, Component resetButtonKey, Supplier<T> defaultValue) {
        this(fieldName, original, resetButtonKey, defaultValue, null);
    }

    protected FullWidthTextFieldEntry(Component fieldName, T original, Component resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Component[]>> tooltipSupplier) {
        this(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, false);
    }


    protected FullWidthTextFieldEntry(Component fieldName, T original, Component resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Component[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.isSelected = false;
        this.defaultValue = defaultValue;
        this.original = original;
        this.textFieldWidget = new EditBox(Minecraft.getInstance().font, 0, 0, 148, 18, Component.empty()) {
            public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int int_1, int int_2, float float_1) {
                this.setFocused(FullWidthTextFieldEntry.this.isSelected && FullWidthTextFieldEntry.this.getFocused() == this);
                FullWidthTextFieldEntry.this.textFieldPreRender(this);
                super.extractWidgetRenderState(graphics, int_1, int_2, float_1);
            }

            public void insertText(@NonNull String string_1) {
                super.insertText(FullWidthTextFieldEntry.this.stripAddText(string_1));
            }
        };
        this.textFieldWidget.setMaxLength(999999);
        this.textFieldWidget.setValue(String.valueOf(original));
        this.textFieldWidget.moveCursorToStart(false);
        this.resetButton = Button.builder(resetButtonKey, (widget) -> this.textFieldWidget.setValue(String.valueOf(defaultValue.get()))).bounds(0, 0, Minecraft.getInstance().font.width(resetButtonKey) + 6, 20).build();
        this.widgets = Lists.newArrayList(this.textFieldWidget, this.resetButton);
    }

    protected static void setTextFieldWidth(EditBox widget, int width) {
        widget.setWidth(width);
    }

    public boolean isEdited() {
        return this.isChanged(this.original, this.textFieldWidget.getValue());
    }

    protected boolean isChanged(T original, String s) {
        return !String.valueOf(original).equals(s);
    }

    public void setValue(String s) {
        this.textFieldWidget.setValue(String.valueOf(s));
    }

    protected String stripAddText(String s) {
        return s;
    }

    protected void textFieldPreRender(EditBox widget) {
        widget.setTextColor(this.getConfigError().isPresent() ? ERROR_TEXT_COLOR : TEXT_COLOR);
    }

    public void updateSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.resetButton.active = this.isEditable() && this.getDefaultValue().isPresent() && !this.isMatchDefault(this.textFieldWidget.getValue());
        this.resetButton.setY(y);
        this.textFieldWidget.setEditable(this.isEditable());
        this.textFieldWidget.setY(y + 1);
        Font textRenderer = Minecraft.getInstance().font;

        int resetButtonWidth = this.resetButton.getWidth();
        if (textRenderer.isBidirectional()) {
            this.resetButton.setX(x);
            this.textFieldWidget.setX(x + resetButtonWidth);
        } else {
            this.resetButton.setX(x + entryWidth - resetButtonWidth);
            this.textFieldWidget.setX(x + TEXT_FIELD_PADDING);
        }

        setTextFieldWidth(this.textFieldWidget, entryWidth - resetButtonWidth - 2 * TEXT_FIELD_PADDING);
        this.resetButton.extractRenderState(graphics, mouseX, mouseY, delta);
        this.textFieldWidget.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    protected boolean isMatchDefault(String text) {
        Optional<T> defaultValue = this.getDefaultValue();
        return defaultValue.isPresent() && text.equals(defaultValue.get().toString());
    }

    public Optional<T> getDefaultValue() {
        return this.defaultValue == null ? Optional.empty() : Optional.ofNullable(this.defaultValue.get());
    }

    public List<? extends GuiEventListener> children() {
        return this.widgets;
    }

    public List<? extends NarratableEntry> narratables() {
        return this.widgets;
    }
}
