package dev.chestwise.core;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ItemDescriptor(
    ItemIdentity identity,
    String displayName,
    Set<String> tags,
    int registryOrder
) {
    public ItemDescriptor {
        Objects.requireNonNull(identity, "identity");
        displayName = Objects.requireNonNull(displayName, "displayName");
        tags = Set.copyOf(Objects.requireNonNull(tags, "tags").stream()
            .map(tag -> tag.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet()));
    }
}

