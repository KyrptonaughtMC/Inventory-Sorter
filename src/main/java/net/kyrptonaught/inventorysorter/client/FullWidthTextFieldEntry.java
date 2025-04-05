package net.kyrptonaught.inventorysorter.client;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/*
  "in memory of layout systems that never learned to stretch properly" - chatgpt's reaction to this file

  All of this is a copy of the original TextFieldEntry class, but with the width set to the full width of the screen.
  That's it...
 */
@Environment(EnvType.CLIENT)
public abstract class FullWidthTextFieldEntry<T> extends TooltipListEntry<T> {
    protected TextFieldWidget textFieldWidget;
    protected ButtonWidget resetButton;
    protected Supplier<T> defaultValue;
    protected T original;
    protected List<ClickableWidget> widgets;
    private boolean isSelected;

    protected FullWidthTextFieldEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue) {
        this(fieldName, original, resetButtonKey, defaultValue, (Supplier) null);
    }

    protected FullWidthTextFieldEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, false);
    }


    protected FullWidthTextFieldEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.isSelected = false;
        this.defaultValue = defaultValue;
        this.original = original;
        this.textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 148, 18, Text.empty()) {
            public void renderWidget(DrawContext graphics, int int_1, int int_2, float float_1) {
                this.setFocused(FullWidthTextFieldEntry.this.isSelected && FullWidthTextFieldEntry.this.getFocused() == this);
                FullWidthTextFieldEntry.this.textFieldPreRender(this);
                super.renderWidget(graphics, int_1, int_2, float_1);
            }

            public void write(String string_1) {
                super.write(FullWidthTextFieldEntry.this.stripAddText(string_1));
            }
        };
        this.textFieldWidget.setMaxLength(999999);
        this.textFieldWidget.setText(String.valueOf(original));
        this.textFieldWidget.setCursorToStart(false);
        this.resetButton = ButtonWidget.builder(resetButtonKey, (widget) -> this.textFieldWidget.setText(String.valueOf(defaultValue.get()))).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20).build();
        this.widgets = Lists.newArrayList(new ClickableWidget[]{this.textFieldWidget, this.resetButton});
    }

    public boolean isEdited() {
        return this.isChanged(this.original, this.textFieldWidget.getText());
    }

    protected boolean isChanged(T original, String s) {
        return !String.valueOf(original).equals(s);
    }

    protected static void setTextFieldWidth(TextFieldWidget widget, int width) {
        widget.setWidth(width);
    }

    public void setValue(String s) {
        this.textFieldWidget.setText(String.valueOf(s));
    }

    protected String stripAddText(String s) {
        return s;
    }

    protected void textFieldPreRender(TextFieldWidget widget) {
        widget.setEditableColor(this.getConfigError().isPresent() ? 16733525 : 14737632);
    }

    public void updateSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    /*
        I made the executive decision to not use the label for the field.
        In this particular mod, the field is nested inside a subcategory, which already has a label,
        and it looks much better this way.

        If you do want labels, uncomment all the lines that are commented out in this method.
     */
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
//        Window window = MinecraftClient.getInstance().getWindow();
        this.resetButton.active = this.isEditable() && this.getDefaultValue().isPresent() && !this.isMatchDefault(this.textFieldWidget.getText());
        this.resetButton.setY(y);
        this.textFieldWidget.setEditable(this.isEditable());
        this.textFieldWidget.setY(y + 1);
//        Text displayedFieldName = this.getDisplayedFieldName();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int labelWidth = 0;
//        int labelWidth = textRenderer.getWidth(displayedFieldName.asOrderedText());
        int padding = 6;
        if (textRenderer.isRightToLeft()) {
//            graphics.drawTextWithShadow(textRenderer, displayedFieldName.asOrderedText(), window.getScaledWidth() - x - textRenderer.getWidth(displayedFieldName), y + padding, this.getPreferredTextColor());
            this.resetButton.setX(x);
            this.textFieldWidget.setX(x + this.resetButton.getWidth());
        } else {
//            graphics.drawTextWithShadow(textRenderer, displayedFieldName.asOrderedText(), x, y + padding, this.getPreferredTextColor());
            this.resetButton.setX(x + entryWidth - this.resetButton.getWidth());

            this.textFieldWidget.setX(x + labelWidth + padding);
        }

        setTextFieldWidth(this.textFieldWidget, entryWidth - labelWidth - this.resetButton.getWidth() - 2 * padding);
        this.resetButton.render(graphics, mouseX, mouseY, delta);
        this.textFieldWidget.render(graphics, mouseX, mouseY, delta);
    }

    protected boolean isMatchDefault(String text) {
        Optional<T> defaultValue = this.getDefaultValue();
        return defaultValue.isPresent() && text.equals(defaultValue.get().toString());
    }

    public Optional<T> getDefaultValue() {
        return this.defaultValue == null ? Optional.empty() : Optional.ofNullable(this.defaultValue.get());
    }

    public List<? extends Element> children() {
        return this.widgets;
    }

    public List<? extends Selectable> narratables() {
        return this.widgets;
    }
}
