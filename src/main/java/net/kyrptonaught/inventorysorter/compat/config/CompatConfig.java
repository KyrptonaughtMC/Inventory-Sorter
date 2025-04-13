package net.kyrptonaught.inventorysorter.compat.config;

import java.util.ArrayList;
import java.util.List;

public class CompatConfig {
    public String customCompatibilityListDownloadUrl = "";
    public List<String> preventSortForScreens = new ArrayList<>();
    public List<String> hideButtonsForScreens = new ArrayList<>();


    public void disableButtonForScreen(String screenId) {
        if (!hideButtonsForScreens.contains(screenId)) {
            hideButtonsForScreens.add(screenId);
        }
    }

    public void enableButtonForScreen(String screenId) {
        hideButtonsForScreens.remove(screenId);
    }

    public void disableSortForScreen(String screenId) {
        if (!preventSortForScreens.contains(screenId)) {
            preventSortForScreens.add(screenId);
        }
    }

    public void enableSortForScreen(String screenId) {
        preventSortForScreens.remove(screenId);
    }
}
