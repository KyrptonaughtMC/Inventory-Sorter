package net.kyrptonaught.inventorysorter.compat.sources;

import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class CommandLoader extends OnlineData {

    private RemoteConfigData remoteData = new RemoteConfigData();

    public CommandLoader(String urlFromCommand) {
        try {
            URL url = URI.create(urlFromCommand).toURL();
            this.remoteData = RemoteConfigData.downloadFrom(url);
        } catch (Exception e) {
            LOGGER.error("Not a valid URL in the config file: {}", urlFromCommand);
        }
    }

    @Override
    public Set<Identifier> getPreventSort() {
        return remoteData.preventSortForScreens.stream().map(Identifier::of).collect(Collectors.toSet());
    }

    @Override
    public Set<Identifier> getShouldHideSortButtons() {
        return remoteData.hideButtonsForScreens.stream().map(Identifier::of).collect(Collectors.toSet());
    }
}
