package dev.chestwise.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DepositPlanner {
    private DepositPlanner() {
    }

    public static DepositPlan plan(List<PlayerStack> player, List<StorageView> sources, DepositMode mode) {
        Set<ItemIdentity> represented = new HashSet<>();
        Map<SlotKey, VirtualSlot> virtualSlots = new HashMap<>();
        for (StorageView source : sources) {
            for (StorageSlot slot : source.slots()) {
                virtualSlots.put(
                    new SlotKey(source.sourceId(), slot.index()),
                    new VirtualSlot(slot.count() == 0 ? null : slot.item().identity(), slot.count(), slot.capacity())
                );
                if (slot.count() > 0) {
                    represented.add(slot.item().identity());
                }
            }
        }

        List<InsertionStep> steps = new ArrayList<>();
        long planned = 0;
        for (PlayerStack stack : player) {
            if (stack.protectedSlot() || mode == DepositMode.MATCHING && !represented.contains(stack.identity())) {
                continue;
            }
            long remaining = stack.count();
            remaining = fill(stack, sources, virtualSlots, steps, remaining, Pass.PARTIAL_MATCH);
            remaining = fill(stack, sources, virtualSlots, steps, remaining, Pass.SOURCE_MATCH);
            if (mode == DepositMode.ALL_ELIGIBLE) {
                remaining = fill(stack, sources, virtualSlots, steps, remaining, Pass.ANY_EMPTY);
            }
            planned += stack.count() - remaining;
        }
        return new DepositPlan(steps, planned);
    }

    private static long fill(
        PlayerStack player,
        List<StorageView> sources,
        Map<SlotKey, VirtualSlot> virtualSlots,
        List<InsertionStep> output,
        long remaining,
        Pass pass
    ) {
        for (StorageView source : sources) {
            boolean sourceHasItem = source.slots().stream()
                .map(slot -> virtualSlots.get(new SlotKey(source.sourceId(), slot.index())))
                .anyMatch(slot -> player.identity().equals(slot.identity));
            for (StorageSlot slot : source.slots()) {
                if (remaining == 0) {
                    return 0;
                }
                SlotKey key = new SlotKey(source.sourceId(), slot.index());
                VirtualSlot current = virtualSlots.get(key);
                boolean matches = switch (pass) {
                    case PARTIAL_MATCH -> player.identity().equals(current.identity) && current.count > 0;
                    case SOURCE_MATCH -> current.count == 0 && sourceHasItem;
                    case ANY_EMPTY -> current.count == 0;
                };
                if (!matches) {
                    continue;
                }
                long amount = Math.min(remaining, current.capacity - current.count);
                if (amount > 0) {
                    output.add(new InsertionStep(player.slot(), source.sourceId(), slot.index(), source.revision(), amount));
                    current.identity = player.identity();
                    current.count += amount;
                    remaining -= amount;
                }
            }
        }
        return remaining;
    }

    private enum Pass {
        PARTIAL_MATCH,
        SOURCE_MATCH,
        ANY_EMPTY
    }

    private record SlotKey(String source, int slot) {
    }

    private static final class VirtualSlot {
        private ItemIdentity identity;
        private long count;
        private final long capacity;

        private VirtualSlot(ItemIdentity identity, long count, long capacity) {
            this.identity = identity;
            this.count = count;
            this.capacity = capacity;
        }
    }
}
