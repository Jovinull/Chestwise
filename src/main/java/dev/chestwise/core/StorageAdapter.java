package dev.chestwise.core;

import java.util.Optional;

/** Platform boundary that turns a loaded world object into a storage source. */
public interface StorageAdapter<C> {
    Optional<StorageSource> adapt(C candidate);
}

