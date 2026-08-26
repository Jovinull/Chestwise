package dev.chestwise.core;

/** Loader-owned physical inventory. Core code never sees capabilities or transfer APIs. */
public interface StorageSource {
    String sourceId();

    StorageView snapshot();

    long simulateExtract(int slot, ItemIdentity identity, long maximum);

    long extract(int slot, ItemIdentity identity, long maximum);

    boolean isValid();
}
