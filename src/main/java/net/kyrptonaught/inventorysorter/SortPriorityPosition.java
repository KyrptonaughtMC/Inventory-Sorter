package net.kyrptonaught.inventorysorter;

public enum SortPriorityPosition {
    FIRST,
    DEFAULT,
    LAST;

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
