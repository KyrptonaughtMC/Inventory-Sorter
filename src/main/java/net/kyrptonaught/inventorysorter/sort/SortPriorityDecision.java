package net.kyrptonaught.inventorysorter.sort;

/**
 * Result produced by a matching priority-rule handler.
 *
 * Ignore decisions are exclusion decisions, not ordering decisions. Callers must check
 * {@link #ignoresStack()} before asking for {@link #priorityKey()}.
 */
public record SortPriorityDecision(SortPriorityPosition position, int ruleOrder) {
    public boolean ignoresStack() {
        return position == SortPriorityPosition.IGNORE;
    }

    public PriorityKey priorityKey() {
        return new PriorityKey(sortBucket(), ruleOrder);
    }

    private int sortBucket() {
        return switch (position) {
            case FIRST -> 0;
            case DEFAULT -> 1;
            case LAST -> 2;
            case IGNORE -> throw new IllegalStateException("Ignore is not a sortable priority position");
        };
    }

    public record PriorityKey(int bucket, int ruleOrder) implements Comparable<PriorityKey> {
        @Override
        public int compareTo(PriorityKey other) {
            int bucketComparison = Integer.compare(bucket, other.bucket);
            if (bucketComparison != 0) {
                return bucketComparison;
            }
            return Integer.compare(ruleOrder, other.ruleOrder);
        }
    }
}
