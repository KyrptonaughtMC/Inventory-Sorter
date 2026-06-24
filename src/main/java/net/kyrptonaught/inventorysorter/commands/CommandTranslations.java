package net.kyrptonaught.inventorysorter.commands;

import net.kyrptonaught.inventorysorter.compat.ServerComponent;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandTranslations {

    static Component getOffMessage(ServerPlayer player, String key) {
        return getFeedbackMessageForState(player, key, false);
    }

    static Component getOnMessage(ServerPlayer player, String key) {
        return getFeedbackMessageForState(player, key, true);
    }

    public static Component toggleState(ServerPlayer player, boolean state) {
        if (state) {
            return ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.toggle.enabled");
        }

        return ServerComponent.lang(player.clientInformation().language()).translate("inventorysorter.toggle.disabled");
    }

    public static Component getFeedbackMessageForState(ServerPlayer player, String key, boolean state) {
        return ServerComponent.lang(player.clientInformation().language()).translate(key, toggleState(player, state));
    }

    public static Component playerRequired() {
        return ServerComponent.translate("inventorysorter.cmd.player-required");
    }
}
