package net.kyrptonaught.inventorysorter;

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

    int sortBucket() {
        return switch (this) {
            case FIRST -> 0;
            case DEFAULT -> 1;
            case LAST -> 2;
            case IGNORE -> throw new IllegalStateException("Ignore is not a sortable priority position");
        };
    }
}
