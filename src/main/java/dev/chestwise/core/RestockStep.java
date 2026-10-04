package dev.chestwise.core;

import java.util.List;
import java.util.Objects;

public record RestockStep(
    ItemIdentity identity,
    long currentCount,
    long desiredCount,
    long plannedCount,
    List<RestockInsertion> insertions
) {
    public RestockStep {
        Objects.requireNonNull(identity, "identity");
        insertions = List.copyOf(insertions);
        if (currentCount < 0 || desiredCount < 1 || plannedCount < 0 || plannedCount > desiredCount - Math.min(currentCount, desiredCount)
            || insertions.stream().mapToLong(RestockInsertion::count).sum() != plannedCount) {
            throw new IllegalArgumentException("Invalid restock step");
        }
    }
}
