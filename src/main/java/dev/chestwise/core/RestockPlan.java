package dev.chestwise.core;

import java.util.List;

public record RestockPlan(List<RestockStep> steps) {
    public RestockPlan {
        steps = List.copyOf(steps);
    }

    public long plannedItems() {
        return steps.stream().mapToLong(RestockStep::plannedCount).sum();
    }
}
