package dev.chestwise.core;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ItemSearch {
    private ItemSearch() {
    }

    public static List<IndexedItem> apply(List<IndexedItem> input, SearchQuery query, SortMode mode) {
        return input.stream().filter(item -> matches(item.descriptor(), query)).sorted(comparator(mode)).toList();
    }

    static boolean matches(ItemDescriptor item, SearchQuery query) {
        String name = item.displayName().toLowerCase(Locale.ROOT);
        String id = item.identity().itemId();
        boolean termsMatch = query.terms().stream().allMatch(term -> name.contains(term) || id.contains(term));
        boolean namespacesMatch = query.namespaces().isEmpty()
            || query.namespaces().stream().anyMatch(item.identity().namespace()::equals);
        boolean tagsMatch = query.tags().stream().allMatch(item.tags()::contains);
        return termsMatch && namespacesMatch && tagsMatch;
    }

    private static Comparator<IndexedItem> comparator(SortMode mode) {
        Comparator<IndexedItem> identity = Comparator.comparing(item -> item.descriptor().identity());
        return switch (mode) {
            case QUANTITY -> Comparator.comparingLong(IndexedItem::totalCount).reversed().thenComparing(identity);
            case NAME -> Comparator.comparing(
                (IndexedItem item) -> item.descriptor().displayName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(identity);
            case NAMESPACE -> Comparator.comparing((IndexedItem item) -> item.descriptor().identity().namespace())
                .thenComparing(item -> item.descriptor().displayName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(identity);
            case REGISTRY -> Comparator.comparingInt((IndexedItem item) -> item.descriptor().registryOrder())
                .thenComparing(identity);
        };
    }
}
