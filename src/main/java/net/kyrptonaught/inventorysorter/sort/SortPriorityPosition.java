package net.kyrptonaught.inventorysorter.sort;

public enum SortPriorityPosition {
    FIRST,
    DEFAULT,
    LAST,
    IGNORE;

    public static SortPriorityPosition fromConfigValue(String value) {
        return SortPriorityPosition.valueOf(value.trim().toUpperCase());
    }

    public String configValue() {
        return name().toLowerCase();
    }

    public String getTranslationKey() {
        return "inventorysorter.config.sortPriorityRules.position." + configValue();
    }
}
