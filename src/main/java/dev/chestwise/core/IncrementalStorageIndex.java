package dev.chestwise.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Thread-safe source-level incremental index. */
public final class IncrementalStorageIndex {
    private final Map<String, StorageView> sources = new HashMap<>();
    private final Map<ItemIdentity, MutableEntry> items = new HashMap<>();

    public synchronized boolean reconcile(StorageView updated) {
        StorageView previous = sources.get(updated.sourceId());
        if (previous != null && previous.revision() == updated.revision()) {
            return false;
        }
        if (previous != null) {
            removeContributions(previous);
        }
        sources.put(updated.sourceId(), updated);
        addContributions(updated);
        return true;
    }

    public synchronized boolean invalidate(String sourceId) {
        StorageView removed = sources.remove(sourceId);
        if (removed == null) {
            return false;
        }
        removeContributions(removed);
        return true;
    }

    public synchronized Optional<IndexedItem> get(ItemIdentity identity) {
        MutableEntry entry = items.get(identity);
        return entry == null ? Optional.empty() : Optional.of(entry.freeze());
    }

    public synchronized List<IndexedItem> snapshot() {
        return items.values().stream()
            .map(MutableEntry::freeze)
            .sorted((left, right) -> left.descriptor().identity().compareTo(right.descriptor().identity()))
            .toList();
    }

    public synchronized List<StorageView> sourceSnapshots() {
        return sources.values().stream().sorted((a, b) -> a.sourceId().compareTo(b.sourceId())).toList();
    }

    public synchronized int sourceCount() {
        return sources.size();
    }

    private void addContributions(StorageView view) {
        for (StorageSlot slot : view.slots()) {
            if (slot.count() == 0) {
                continue;
            }
            ItemDescriptor descriptor = slot.item();
            MutableEntry entry = items.computeIfAbsent(descriptor.identity(), ignored -> new MutableEntry(descriptor));
            entry.locations.add(new PhysicalSlotRef(view.sourceId(), slot.index(), view.revision(), slot.count()));
            entry.total += slot.count();
        }
    }

    private void removeContributions(StorageView view) {
        for (StorageSlot slot : view.slots()) {
            if (slot.count() == 0) {
                continue;
            }
            MutableEntry entry = items.get(slot.item().identity());
            if (entry == null) {
                throw new IllegalStateException("Index contribution missing for " + slot.item().identity());
            }
            entry.locations.removeIf(ref -> ref.sourceId().equals(view.sourceId()) && ref.slot() == slot.index());
            entry.total -= slot.count();
            if (entry.total == 0) {
                items.remove(slot.item().identity());
            }
        }
    }

    private static final class MutableEntry {
        private final ItemDescriptor descriptor;
        private final List<PhysicalSlotRef> locations = new ArrayList<>();
        private long total;

        private MutableEntry(ItemDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        private IndexedItem freeze() {
            return new IndexedItem(descriptor, total, locations);
        }
    }
}

