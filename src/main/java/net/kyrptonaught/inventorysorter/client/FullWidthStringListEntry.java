package net.kyrptonaught.inventorysorter.client;

import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FullWidthStringListEntry extends FullWidthTextFieldEntry<String> {

    protected FullWidthStringListEntry(Component fieldName, String original, Component resetButtonKey, Supplier<String> defaultValue) {
        super(fieldName, original, resetButtonKey, defaultValue);
    }

    protected FullWidthStringListEntry(Component fieldName, String original, Component resetButtonKey, Supplier<String> defaultValue, Supplier<Optional<Component[]>> tooltipSupplier) {
        super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier);
    }

    protected FullWidthStringListEntry(Component fieldName, String original, Component resetButtonKey, Supplier<String> defaultValue, Supplier<Optional<Component[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
    }

    protected FullWidthStringListEntry(Component fieldName, String original, Component resetButtonKey, Supplier<String> defaultValue, Supplier<Optional<Component[]>> tooltipSupplier, Consumer<String> saveConsumer) {
        super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier);
        this.saveCallback = saveConsumer;
    }

    @Override
    public String getValue() {
        return textFieldWidget.getValue();
    }
}
