package net.kyrptonaught.inventorysorter.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortModeScrollDebounceTest {
    @Test
    void repeatedChangesSaveAndSyncOnceAfterTheLastQuietPeriod() {
        AtomicLong now = new AtomicLong();
        AtomicInteger saves = new AtomicInteger();
        AtomicInteger syncs = new AtomicInteger();
        SortModeScrollDebounce debounce = new SortModeScrollDebounce(now::get, saves::incrementAndGet, syncs::incrementAndGet);

        debounce.changed();
        now.set(299_000_000L);
        debounce.tick();
        debounce.changed();
        now.set(598_000_000L);
        debounce.tick();
        assertEquals(0, saves.get());
        assertEquals(0, syncs.get());

        now.set(599_000_000L);
        debounce.tick();
        debounce.tick();
        assertEquals(1, saves.get());
        assertEquals(1, syncs.get());
    }

    @Test
    void stoppingFlushesPendingChangeWithoutNetworkSync() {
        AtomicInteger saves = new AtomicInteger();
        AtomicInteger syncs = new AtomicInteger();
        SortModeScrollDebounce debounce = new SortModeScrollDebounce(() -> 0L, saves::incrementAndGet, syncs::incrementAndGet);

        debounce.changed();
        debounce.flushOnStop();
        debounce.flushOnStop();
        assertEquals(1, saves.get());
        assertEquals(0, syncs.get());
    }

    @Test
    void disconnectFlushPreventsDelayedNetworkSync() {
        AtomicLong now = new AtomicLong();
        AtomicInteger saves = new AtomicInteger();
        AtomicInteger syncs = new AtomicInteger();
        SortModeScrollDebounce debounce = new SortModeScrollDebounce(now::get, saves::incrementAndGet, syncs::incrementAndGet);

        debounce.changed();
        debounce.flushOnStop();
        now.set(300_000_000L);
        debounce.tick();

        assertEquals(1, saves.get());
        assertEquals(0, syncs.get());
    }

    @Test
    void ticksAndStoppingWithoutChangesDoNothing() {
        AtomicInteger saves = new AtomicInteger();
        AtomicInteger syncs = new AtomicInteger();
        SortModeScrollDebounce debounce = new SortModeScrollDebounce(() -> 500_000_000L, saves::incrementAndGet, syncs::incrementAndGet);

        debounce.tick();
        debounce.flushOnStop();
        assertEquals(0, saves.get());
        assertEquals(0, syncs.get());
    }
}
