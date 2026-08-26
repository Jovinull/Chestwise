package dev.chestwise.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TransferPlanner {
    private TransferPlanner() {
    }

    public static ExtractionPlan extract(IndexedItem item, long requested) {
        if (requested <= 0) {
            throw new IllegalArgumentException("Requested amount must be positive");
        }
        long remaining = requested;
        List<ExtractionStep> steps = new ArrayList<>();
        List<PhysicalSlotRef> locations = item.locations().stream()
            .sorted(Comparator.comparing(PhysicalSlotRef::sourceId).thenComparingInt(PhysicalSlotRef::slot))
            .toList();
        for (PhysicalSlotRef location : locations) {
            if (remaining == 0) {
                break;
            }
            long amount = Math.min(remaining, location.count());
            steps.add(new ExtractionStep(location, amount));
            remaining -= amount;
        }
        return new ExtractionPlan(item.descriptor().identity(), requested, requested - remaining, steps);
    }
}

