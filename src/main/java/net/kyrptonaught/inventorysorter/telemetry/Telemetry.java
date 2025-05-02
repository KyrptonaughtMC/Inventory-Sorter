package net.kyrptonaught.inventorysorter.telemetry;

import static net.kyrptonaught.inventorysorter.InventorySorterMod.LOGGER;

public class Telemetry {

    Telemetry() {
        this.internal = new gg.meza.telemetry.Telemetry("", "MOD_VERSION_REPL", LOGGER);
    }
}
