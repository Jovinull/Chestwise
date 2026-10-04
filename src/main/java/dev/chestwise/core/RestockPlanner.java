package dev.chestwise.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure planning for best-effort, exact-identity restock operations. */
public final class RestockPlanner {
    private RestockPlanner() {
    }

    public static RestockPlan plan(
        List<RestockTarget> targets,
        List<RestockPlayerSlot> playerSlots,
        Map<ItemIdentity, Long> sourceCounts
    ) {
        if (playerSlots.size() != 36) {
            throw new IllegalArgumentException("Restock requires the 36 main/hotbar inventory slots");
        }
        List<MutableSlot> slots = playerSlots.stream()
            .sorted(Comparator.comparingInt(RestockPlayerSlot::slot))
            .map(MutableSlot::new)
            .toList();
        if (slots.stream().map(slot -> slot.slot).distinct().count() != 36) {
            throw new IllegalArgumentException("Restock inventory slot indices must be unique");
        }

        Set<ItemIdentity> identities = new HashSet<>();
        List<RestockStep> steps = new ArrayList<>();
        for (RestockTarget target : targets) {
            if (!identities.add(target.identity())) {
                throw new IllegalArgumentException("Duplicate exact-identity restock target");
            }
            long current = 0;
            long insertCapacity = 0;
            for (MutableSlot slot : slots) {
                if (target.identity().equals(slot.identity)) {
                    current = safeAdd(current, slot.count);
                    if (!slot.protectedSlot) {
                        insertCapacity = safeAdd(insertCapacity, target.maxStackSize() - slot.count);
                    }
                } else if (slot.identity == null && !slot.protectedSlot) {
                    insertCapacity = safeAdd(insertCapacity, target.maxStackSize());
                }
            }

            long deficit = Math.max(0L, target.desiredCount() - current);
            long available = Math.max(0L, sourceCounts.getOrDefault(target.identity(), 0L));
            long planned = Math.min(deficit, Math.min(insertCapacity, available));
            List<RestockInsertion> insertions = allocate(slots, target, planned);
            steps.add(new RestockStep(target.identity(), current, target.desiredCount(), planned, insertions));
        }
        return new RestockPlan(steps);
    }

    private static List<RestockInsertion> allocate(List<MutableSlot> slots, RestockTarget target, long amount) {
        if (amount == 0) {
            return List.of();
        }
        List<RestockInsertion> insertions = new ArrayList<>();
        long remaining = amount;
        for (int pass = 0; pass < 2 && remaining > 0; pass++) {
            for (MutableSlot slot : slots) {
                if (slot.protectedSlot) {
                    continue;
                }
                boolean matches = pass == 0
                    ? target.identity().equals(slot.identity)
                    : slot.identity == null;
                if (!matches) {
                    continue;
                }
                int space = target.maxStackSize() - slot.count;
                int inserted = (int) Math.min(remaining, Math.max(0, space));
                if (inserted > 0) {
                    insertions.add(new RestockInsertion(slot.slot, inserted));
                    slot.identity = target.identity();
                    slot.count += inserted;
                    remaining -= inserted;
                }
            }
        }
        if (remaining != 0) {
            throw new IllegalStateException("Restock capacity calculation and allocation diverged");
        }
        return insertions;
    }

    private static long safeAdd(long first, long second) {
        return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
    }

    private static final class MutableSlot {
        private final int slot;
        private ItemIdentity identity;
        private int count;
        private final boolean protectedSlot;

        private MutableSlot(RestockPlayerSlot slot) {
            this.slot = slot.slot();
            identity = slot.identity();
            count = slot.count();
            protectedSlot = slot.protectedSlot();
        }
    }
}
