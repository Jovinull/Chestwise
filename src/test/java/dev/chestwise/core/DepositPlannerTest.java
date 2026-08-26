package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DepositPlannerTest {
    private static final ItemDescriptor COBBLE = descriptor("minecraft:cobblestone");
    private static final ItemDescriptor DIAMOND = descriptor("minecraft:diamond");

    @Test
    void matchingSkipsUnrepresentedAndProtectedItems() {
        StorageView storage = new StorageView("barrel", 5, List.of(
            new StorageSlot(0, COBBLE, 60, 64),
            StorageSlot.empty(1, 64)
        ));
        List<PlayerStack> player = List.of(
            new PlayerStack(0, COBBLE.identity(), 16, false),
            new PlayerStack(1, DIAMOND.identity(), 3, false),
            new PlayerStack(2, COBBLE.identity(), 8, true)
        );

        DepositPlan plan = DepositPlanner.plan(player, List.of(storage), DepositMode.MATCHING);

        assertEquals(16, plan.plannedItems());
        assertEquals(4, plan.steps().get(0).amount());
        assertEquals(12, plan.steps().get(1).amount());
    }

    @Test
    void allEligibleUsesExistingStacksThenKnownInventoryThenFallback() {
        StorageView known = new StorageView("a-known", 1, List.of(
            new StorageSlot(0, COBBLE, 63, 64),
            StorageSlot.empty(1, 64)
        ));
        StorageView empty = new StorageView("b-empty", 1, List.of(
            StorageSlot.empty(0, 64),
            StorageSlot.empty(1, 64)
        ));
        List<PlayerStack> player = List.of(
            new PlayerStack(0, COBBLE.identity(), 70, false),
            new PlayerStack(1, DIAMOND.identity(), 2, false)
        );

        DepositPlan plan = DepositPlanner.plan(player, List.of(known, empty), DepositMode.ALL_ELIGIBLE);

        assertEquals(72, plan.plannedItems());
        assertEquals(List.of("a-known", "a-known", "b-empty", "b-empty"),
            plan.steps().stream().map(InsertionStep::sourceId).toList());
        assertEquals(List.of(1L, 64L, 5L),
            plan.steps().subList(0, 3).stream().map(InsertionStep::amount).toList());
    }

    @Test
    void repeatedSameItemCannotOverfillVirtualTarget() {
        StorageView storage = new StorageView("barrel", 1, List.of(StorageSlot.empty(0, 64)));
        DepositPlan plan = DepositPlanner.plan(List.of(
            new PlayerStack(0, COBBLE.identity(), 40, false),
            new PlayerStack(1, COBBLE.identity(), 40, false)
        ), List.of(storage), DepositMode.ALL_ELIGIBLE);

        assertEquals(64, plan.plannedItems());
        assertEquals(List.of(40L, 24L), plan.steps().stream().map(InsertionStep::amount).toList());
    }

    private static ItemDescriptor descriptor(String id) {
        return new ItemDescriptor(ItemIdentity.simple(id), id, Set.of(), 0);
    }
}
