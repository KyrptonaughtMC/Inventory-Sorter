package net.kyrptonaught.inventorysorter.client;

public class ClientServerSupport {
    private volatile Status status = Status.UNKNOWN;

    public boolean isPresent() {
        return status == Status.PRESENT;
    }

    /**
     * Returns whether sort requests should use the server-authoritative path.
     *
     * UNKNOWN deliberately returns false so clients can use local fallback sorting immediately
     * after joining. Once the server presence packet arrives, PRESENT returns true and subsequent
     * sort requests switch to the server path.
     */
    public boolean shouldUseServerSorting() {
        return status == Status.PRESENT;
    }

    public void reset() {
        status = Status.UNKNOWN;
    }

    public void markPresent() {
        status = Status.PRESENT;
    }

    public void markAbsent() {
        status = Status.ABSENT;
    }

    private enum Status {
        UNKNOWN,
        PRESENT,
        ABSENT
    }
}
