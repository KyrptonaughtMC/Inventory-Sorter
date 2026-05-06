package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.compat.config.CompatConfig;
import net.kyrptonaught.inventorysorter.config.NewConfigOptions;
import net.kyrptonaught.inventorysorter.network.*;
import net.kyrptonaught.inventorysorter.platform.NetworkingPlatform;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.*;

public class ClientPacketReceivers {
    private final Supplier<NewConfigOptions> config;
    private final Runnable reloadConfig;
    private final Runnable reloadCompatibility;
    private final ClientServerSupport serverSupport;
    private CompatConfig serverConfig = new CompatConfig();

    public ClientPacketReceivers() {
        this(InventorySorterMod::getConfig, InventorySorterMod::reloadConfig, compatibility::reload);
    }

    ClientPacketReceivers(
            Supplier<NewConfigOptions> config,
            Runnable reloadConfig,
            Runnable reloadCompatibility
    ) {
        this(config, reloadConfig, reloadCompatibility, new ClientServerSupport());
    }

    ClientPacketReceivers(
            Supplier<NewConfigOptions> config,
            Runnable reloadConfig,
            Runnable reloadCompatibility,
            ClientServerSupport serverSupport
    ) {
        this.config = config;
        this.reloadConfig = reloadConfig;
        this.reloadCompatibility = reloadCompatibility;
        this.serverSupport = serverSupport;
    }

    CompatConfig serverConfig() {
        return serverConfig;
    }

    boolean serverIsPresent() {
        return serverSupport.isPresent();
    }

    void resetServerState() {
        serverConfig = new CompatConfig();
        serverSupport.reset();
    }

    void markServerPresent() {
        serverSupport.markPresent();
    }

    void markServerAbsent() {
        serverSupport.markAbsent();
    }

    public void register(NetworkingPlatform networking) {
        networking.registerClientReceivers(
                this::applySortSettings,
                this::applyPlayerSortPrevention,
                this::applyHideButton,
                reloadConfig,
                this::handleLastSeenVersion,
                this::markServerPresent
        );
    }

    void applySortSettings(SortSettings payload) {
        NewConfigOptions currentConfig = config.get();
        currentConfig.enableDoubleClickSort = payload.enableDoubleClick();
        currentConfig.sortIntoBundles = payload.sortIntoBundles();
        currentConfig.sortIntoHotbarBundles = payload.sortIntoHotbarBundles();
        currentConfig.sortType = payload.sortType();
        currentConfig.sortPriorityRules = payload.sortPriorityRules().stream().toList();
        currentConfig.save();
    }

    /*
      This happens when the client connects to a server for the first time.
      It's to sync the server's config to the client if the user has added any sort
      preventions for themselves.
     */
    void applyPlayerSortPrevention(PlayerSortPrevention payload) {
        NewConfigOptions currentConfig = config.get();
        currentConfig.preventSortForScreens.retainAll(payload.preventSortForScreens());
        payload.preventSortForScreens().forEach(currentConfig::disableSortForScreen);
        currentConfig.save();
        reloadCompatibility.run();
    }

    /*
      If the server owners have defined any screens that should have the sort button hidden,
      this is how we sync that to the client and keep it separate from the player's config.
     */
    void applyHideButton(HideButton payload) {
        serverConfig.hideButtonsForScreens = payload.hideButtonForScreens().stream().toList();
        reloadCompatibility.run();
    }

    private void handleLastSeenVersion(LastSeenVersionPacket payload) {
        TranslationReminder.notifyIfOutdated(Minecraft.getInstance(), payload, VERSION);
    }
}
