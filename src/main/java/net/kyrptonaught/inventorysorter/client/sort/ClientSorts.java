package net.kyrptonaught.inventorysorter.client.sort;

import net.kyrptonaught.inventorysorter.SortTarget;

/**
 * Static bridge for UI and mixin code that cannot receive the client sort runtime directly.
 *
 * Runtime ownership belongs to {@link ClientSortRuntime}. Keep this bridge limited to forwarding
 * user sort requests so global access does not grow into another owner of sorting state.
 */
public final class ClientSorts {
    private static ClientSortRuntime runtime;

    private ClientSorts() {
    }

    public static void configure(ClientSortRuntime clientSortRuntime) {
        runtime = clientSortRuntime;
    }

    /**
     * Requests a sort for the current screen.
     *
     * When server support is present this sends the server-authoritative sort packet. While
     * support is unknown or absent, this falls back to client-side click replay so sorting is
     * available immediately after joining. A true result means the request was accepted, not that
     * the inventory has already reached the sorted state.
     */
    public static boolean requestCurrentScreenSort(SortTarget target) {
        return requireRuntime().requestCurrentScreenSort(target);
    }

    private static ClientSortRuntime requireRuntime() {
        if (runtime == null) {
            throw new IllegalStateException("Client sorting was used before client initialization");
        }
        return runtime;
    }
}
