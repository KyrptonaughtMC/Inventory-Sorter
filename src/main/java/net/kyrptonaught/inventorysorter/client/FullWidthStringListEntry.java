package net.kyrptonaught.inventorysorter.client;

import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Supplier;

public class FullWidthStringListEntry extends FullWidthTextFieldEntry<String> {

    protected FullWidthStringListEntry(Text fieldName, String original, Text resetButtonKey, Supplier<String> defaultValue) {
        super(fieldName, original, resetButtonKey, defaultValue);
    }

    protected FullWidthStringListEntry(Text fieldName, String original, Text resetButtonKey, Supplier<String> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier) {
        super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier);
    }

    protected FullWidthStringListEntry(Text fieldName, String original, Text resetButtonKey, Supplier<String> defaultValue, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
    }

    @Override
    public String getValue() {
        return textFieldWidget.getText();
    }
}
