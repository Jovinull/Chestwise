package dev.chestwise.core;

import java.util.Objects;

public record RestockTarget(ItemIdentity identity, int desiredCount, int maxStackSize) {
    public static final int MAX_DESIRED_COUNT = 4096;

    public RestockTarget {
        Objects.requireNonNull(identity, "identity");
        if (desiredCount < 1 || desiredCount > MAX_DESIRED_COUNT || maxStackSize < 1) {
            throw new IllegalArgumentException("Invalid restock target");
        }
    }
}
