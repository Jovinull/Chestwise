package dev.chestwise.core;

import java.util.Objects;
import java.util.Optional;

public record StorageSlot(int index, ItemDescriptor item, long count, long capacity) {
    public StorageSlot {
        if (index < 0 || count < 0 || capacity < 0 || count > capacity) {
            throw new IllegalArgumentException("Invalid storage slot values");
        }
        if (count > 0) {
            Objects.requireNonNull(item, "Non-empty slot needs an item");
        } else if (item != null) {
            throw new IllegalArgumentException("Empty slot cannot carry an item identity");
        }
    }

    public static StorageSlot empty(int index, long capacity) {
        return new StorageSlot(index, null, 0, capacity);
    }

    public Optional<ItemDescriptor> itemOptional() {
        return Optional.ofNullable(item);
    }

    public long availableSpaceFor(ItemIdentity identity) {
        if (count == 0 || item.identity().equals(identity)) {
            return capacity - count;
        }
        return 0;
    }
}

