package dev.chestwise.core;

import java.util.List;
import java.util.Objects;

public record IndexedItem(ItemDescriptor descriptor, long totalCount, List<PhysicalSlotRef> locations) {
    public IndexedItem {
        Objects.requireNonNull(descriptor, "descriptor");
        locations = List.copyOf(Objects.requireNonNull(locations, "locations"));
        if (totalCount <= 0 || locations.stream().mapToLong(PhysicalSlotRef::count).sum() != totalCount) {
            throw new IllegalArgumentException("Indexed total does not match locations");
        }
    }
}

