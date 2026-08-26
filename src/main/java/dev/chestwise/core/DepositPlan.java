package dev.chestwise.core;

import java.util.List;

public record DepositPlan(List<InsertionStep> steps, long plannedItems) {
    public DepositPlan {
        steps = List.copyOf(steps);
        if (plannedItems < 0 || steps.stream().mapToLong(InsertionStep::amount).sum() != plannedItems) {
            throw new IllegalArgumentException("Invalid deposit plan");
        }
    }
}

