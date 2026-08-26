package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IncrementalStorageIndexTest {
    private static final ItemDescriptor IRON = new ItemDescriptor(
        ItemIdentity.simple("minecraft:iron_ingot"), "Iron Ingot", Set.of("minecraft:ingots/iron"), 1);

    @Test
    void updatesOnlyChangedSourceAndInvalidatesRemovedInventory() {
        IncrementalStorageIndex index = new IncrementalStorageIndex();
        StorageView first = view("chest:a", 1, 32);

        assertTrue(index.reconcile(first));
        assertFalse(index.reconcile(first));
        assertEquals(32, index.get(IRON.identity()).orElseThrow().totalCount());

        assertTrue(index.reconcile(view("chest:a", 2, 7)));
        assertEquals(7, index.get(IRON.identity()).orElseThrow().totalCount());
        assertEquals(1, index.sourceCount());

        assertTrue(index.invalidate("chest:a"));
        assertTrue(index.snapshot().isEmpty());
        assertFalse(index.invalidate("chest:a"));
    }

    @Test
    void aggregatesExactIdentityAcrossPhysicalLocations() {
        IncrementalStorageIndex index = new IncrementalStorageIndex();
        index.reconcile(view("chest:a", 1, 32));
        index.reconcile(view("barrel:b", 3, 17));

        IndexedItem indexed = index.get(IRON.identity()).orElseThrow();
        assertEquals(49, indexed.totalCount());
        assertEquals(2, indexed.locations().size());
    }

    private static StorageView view(String id, long revision, long count) {
        return new StorageView(id, revision, List.of(new StorageSlot(0, IRON, count, 64)));
    }
}

