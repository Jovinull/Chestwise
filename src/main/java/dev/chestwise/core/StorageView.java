package dev.chestwise.core;

import java.util.List;
import java.util.Objects;

/** Immutable snapshot of one physical, already de-duplicated inventory. */
public record StorageView(String sourceId, long revision, List<StorageSlot> slots) {
    public StorageView {
        sourceId = Objects.requireNonNull(sourceId, "sourceId");
        slots = List.copyOf(Objects.requireNonNull(slots, "slots"));
        if (sourceId.isBlank() || revision < 0) {
            throw new IllegalArgumentException("Invalid storage view");
        }
        for (int expected = 0; expected < slots.size(); expected++) {
            if (slots.get(expected).index() != expected) {
                throw new IllegalArgumentException("Slots must be contiguous and ordered");
            }
        }
    }
}

