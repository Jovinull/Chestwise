package dev.chestwise.core;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/** Exact, version-neutral identity supplied by a Minecraft version bridge. */
public final class ItemIdentity implements Comparable<ItemIdentity> {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private static final AtomicLong NEXT_TIE_BREAKER = new AtomicLong();

    private final String itemId;
    private final String canonicalData;
    private final Object exactData;
    private final long tieBreaker;

    public ItemIdentity(String itemId, String canonicalData) {
        this(itemId, canonicalData, canonicalData);
    }

    private ItemIdentity(String itemId, String canonicalData, Object exactData) {
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.canonicalData = Objects.requireNonNull(canonicalData, "canonicalData");
        this.exactData = Objects.requireNonNull(exactData, "exactData");
        this.tieBreaker = NEXT_TIE_BREAKER.getAndIncrement();
        if (!ID.matcher(itemId).matches()) {
            throw new IllegalArgumentException("Not a namespaced item id: " + itemId);
        }
    }

    /** Uses semantic equality for an opaque, version-specific immutable data object. */
    public static ItemIdentity exact(String itemId, Object exactData, String canonicalData) {
        return new ItemIdentity(itemId, canonicalData, exactData);
    }

    public static ItemIdentity simple(String itemId) {
        return new ItemIdentity(itemId, "");
    }

    public String itemId() {
        return itemId;
    }

    public String namespace() {
        return itemId.substring(0, itemId.indexOf(':'));
    }

    public String path() {
        return itemId.substring(itemId.indexOf(':') + 1);
    }

    public String canonicalData() {
        return canonicalData;
    }

    @Override
    public int compareTo(ItemIdentity other) {
        int idComparison = itemId.compareTo(other.itemId);
        if (idComparison != 0) {
            return idComparison;
        }
        if (exactData.equals(other.exactData)) {
            return 0;
        }
        int canonicalComparison = canonicalData.compareTo(other.canonicalData);
        if (canonicalComparison != 0) {
            return canonicalComparison;
        }
        int hashComparison = Integer.compare(exactData.hashCode(), other.exactData.hashCode());
        return hashComparison != 0 ? hashComparison : Long.compare(tieBreaker, other.tieBreaker);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ItemIdentity other
            && itemId.equals(other.itemId)
            && exactData.equals(other.exactData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, exactData);
    }

    @Override
    public String toString() {
        return canonicalData.isEmpty() ? itemId : itemId + canonicalData;
    }
}
