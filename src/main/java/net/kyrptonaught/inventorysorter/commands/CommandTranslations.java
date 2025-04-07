package net.kyrptonaught.inventorysorter.commands;

import net.minecraft.text.Text;

public class CommandTranslations {

    static Text getOffMessage(String key) {
        return getFeedbackMessageForState(key, false);
    }

    static Text getOnMessage(String key) {
        return getFeedbackMessageForState(key, true);
    }

    public static Text toggleState(boolean state) {
        if (state) {
            return Text.translatable("inventorysorter.toggle.enabled");
        }

        return Text.translatable("inventorysorter.toggle.disabled");
    }

    public static Text getFeedbackMessageForState(String key, boolean state) {
        return Text.translatable(key, toggleState(state));
    }

    public static Text playerRequired() {
        return Text.translatable("inventorysorter.cmd.player-required");
    }
}
