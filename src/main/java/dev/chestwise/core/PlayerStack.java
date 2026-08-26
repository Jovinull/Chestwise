package dev.chestwise.core;

import java.util.Objects;

public record PlayerStack(int slot, ItemIdentity identity, long count, boolean protectedSlot) {
    public PlayerStack {
        Objects.requireNonNull(identity, "identity");
        if (slot < 0 || count <= 0) {
            throw new IllegalArgumentException("Invalid player stack");
        }
    }
}

