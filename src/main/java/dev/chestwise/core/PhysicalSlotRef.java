package dev.chestwise.core;

import java.util.Objects;

public record PhysicalSlotRef(String sourceId, int slot, long sourceRevision, long count) {
    public PhysicalSlotRef {
        sourceId = Objects.requireNonNull(sourceId, "sourceId");
        if (sourceId.isBlank() || slot < 0 || sourceRevision < 0 || count <= 0) {
            throw new IllegalArgumentException("Invalid physical slot reference");
        }
    }
}

