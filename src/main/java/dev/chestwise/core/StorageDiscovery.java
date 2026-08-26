package dev.chestwise.core;

import java.util.Collection;

/** Bounded discovery. Implementations must never load a chunk. */
public interface StorageDiscovery<T> {
    Collection<StorageSource> discover(T terminal, int radius, int maximumSources);
}

