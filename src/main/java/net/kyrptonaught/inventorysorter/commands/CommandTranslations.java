package net.kyrptonaught.inventorysorter.commands;

import net.minecraft.network.chat.Component;

public class CommandTranslations {

    static Component getOffMessage(String key) {
        return getFeedbackMessageForState(key, false);
    }

    static Component getOnMessage(String key) {
        return getFeedbackMessageForState(key, true);
    }

    public static Component toggleState(boolean state) {
        if (state) {
            return Component.translatable("inventorysorter.toggle.enabled");
        }

        return Component.translatable("inventorysorter.toggle.disabled");
    }

    public static Component getFeedbackMessageForState(String key, boolean state) {
        return Component.translatable(key, toggleState(state));
    }

    public static Component playerRequired() {
        return Component.translatable("inventorysorter.cmd.player-required");
    }
}
