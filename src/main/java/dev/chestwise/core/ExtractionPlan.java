package dev.chestwise.core;

import java.util.List;

public record ExtractionPlan(ItemIdentity identity, long requested, long planned, List<ExtractionStep> steps) {
    public ExtractionPlan {
        steps = List.copyOf(steps);
        if (requested <= 0 || planned < 0 || planned > requested
            || steps.stream().mapToLong(ExtractionStep::amount).sum() != planned) {
            throw new IllegalArgumentException("Invalid extraction plan");
        }
    }

    public boolean complete() {
        return requested == planned;
    }
}

