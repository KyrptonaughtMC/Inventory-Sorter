package net.kyrptonaught.inventorysorter.client;

import java.util.function.LongSupplier;

final class SortModeScrollDebounce {
    private static final long QUIET_PERIOD_NANOS = 300_000_000L;

    private final LongSupplier nanoTime;
    private final Runnable save;
    private final Runnable sync;
    private long lastChangeNanos;
    private boolean pending;

    SortModeScrollDebounce(LongSupplier nanoTime, Runnable save, Runnable sync) {
        this.nanoTime = nanoTime;
        this.save = save;
        this.sync = sync;
    }

    void changed() {
        lastChangeNanos = nanoTime.getAsLong();
        pending = true;
    }

    void tick() {
        if (pending && nanoTime.getAsLong() - lastChangeNanos >= QUIET_PERIOD_NANOS) {
            persist(true);
        }
    }

    void flushOnStop() {
        if (pending) {
            persist(false);
        }
    }

    private void persist(boolean syncToServer) {
        pending = false;
        save.run();
        if (syncToServer) {
            sync.run();
        }
    }
}
