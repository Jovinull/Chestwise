package dev.chestwise.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Ordered, bounded target collection with exact-identity upsert semantics. */
public final class RestockTargetBook {
    public static final int MAX_TARGETS = 36;

    private final List<RestockTarget> targets = new ArrayList<>();

    public List<RestockTarget> entries() {
        return List.copyOf(targets);
    }

    public Change set(ItemIdentity identity, int desiredCount, int maxStackSize) {
        Objects.requireNonNull(identity, "identity");
        if (maxStackSize < 1 || desiredCount < 1
            || desiredCount > Math.min(RestockTarget.MAX_DESIRED_COUNT, (long) maxStackSize * MAX_TARGETS)) {
            return Change.INVALID;
        }
        RestockTarget updated = new RestockTarget(identity, desiredCount, maxStackSize);
        for (int index = 0; index < targets.size(); index++) {
            if (targets.get(index).identity().equals(identity)) {
                targets.set(index, updated);
                return Change.UPDATED;
            }
        }
        if (targets.size() >= MAX_TARGETS) {
            return Change.FULL;
        }
        targets.add(updated);
        return Change.ADDED;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= targets.size()) {
            return false;
        }
        targets.remove(index);
        return true;
    }

    public enum Change {
        ADDED,
        UPDATED,
        INVALID,
        FULL
    }
}
