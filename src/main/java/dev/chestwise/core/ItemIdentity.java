package dev.chestwise.core;

import java.util.Objects;
import java.util.regex.Pattern;

/** Exact, version-neutral identity supplied by a Minecraft version bridge. */
public final class ItemIdentity implements Comparable<ItemIdentity> {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    private final String itemId;
    private final String canonicalData;

    public ItemIdentity(String itemId, String canonicalData) {
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.canonicalData = Objects.requireNonNull(canonicalData, "canonicalData");
        if (!ID.matcher(itemId).matches()) {
            throw new IllegalArgumentException("Not a namespaced item id: " + itemId);
        }
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
        return idComparison != 0 ? idComparison : canonicalData.compareTo(other.canonicalData);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ItemIdentity other
            && itemId.equals(other.itemId)
            && canonicalData.equals(other.canonicalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, canonicalData);
    }

    @Override
    public String toString() {
        return canonicalData.isEmpty() ? itemId : itemId + canonicalData;
    }
}

