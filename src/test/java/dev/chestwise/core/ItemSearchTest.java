package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ItemSearchTest {
    @Test
    void supportsTextNamespaceAndTagFiltersTogether() {
        IndexedItem oak = indexed("minecraft:oak_log", "Oak Log", Set.of("minecraft:logs"), 80, 1);
        IndexedItem zinc = indexed("create:zinc_ingot", "Zinc Ingot", Set.of("c:ingots/zinc"), 4, 2);
        IndexedItem createOak = indexed("create:oak_window", "Oak Window", Set.of(), 2, 3);
        List<IndexedItem> items = List.of(oak, zinc, createOak);

        assertEquals(List.of(oak), ItemSearch.apply(items, SearchQuery.parse("oak #logs"), SortMode.NAME));
        assertEquals(List.of(createOak), ItemSearch.apply(items, SearchQuery.parse("oak @create"), SortMode.NAME));
        assertEquals(List.of(zinc), ItemSearch.apply(items, SearchQuery.parse("@create zinc"), SortMode.NAME));
    }

    @Test
    void quantitySortIsDescendingAndStableByIdentity() {
        IndexedItem low = indexed("minecraft:apple", "Apple", Set.of(), 2, 1);
        IndexedItem high = indexed("minecraft:stone", "Stone", Set.of(), 64, 2);
        assertEquals(List.of(high, low), ItemSearch.apply(List.of(low, high), SearchQuery.parse(""), SortMode.QUANTITY));
    }

    private static IndexedItem indexed(String id, String name, Set<String> tags, long count, int order) {
        ItemDescriptor descriptor = new ItemDescriptor(ItemIdentity.simple(id), name, tags, order);
        return new IndexedItem(descriptor, count, List.of(new PhysicalSlotRef("source", order, 1, count)));
    }
}

