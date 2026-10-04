package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RestockPlannerTest {
    private static final ItemIdentity TORCH = ItemIdentity.simple("minecraft:torch");
    private static final ItemIdentity COBBLE = ItemIdentity.simple("minecraft:cobblestone");
    private static final ItemIdentity BREAD = ItemIdentity.simple("minecraft:bread");
    private static final ItemIdentity ARROW = ItemIdentity.simple("minecraft:arrow");

    @Test
    void satisfiedTargetDoesNotExtract() {
        RestockPlan plan = plan(List.of(target(TORCH, 64, 64)), inventory(slot(0, TORCH, 64, false)), Map.of(TORCH, 500L));
        assertEquals(0, plan.plannedItems());
    }

    @Test
    void fillsOnlyTheDeficitAndAllocatesAcrossStacks() {
        List<RestockPlayerSlot> player = inventory(slot(0, COBBLE, 64, false), slot(1, COBBLE, 64, false), slot(2, COBBLE, 20, false));
        RestockStep step = onlyStep(plan(List.of(target(COBBLE, 256, 64)), player, Map.of(COBBLE, 108L)));
        assertEquals(148, step.currentCount());
        assertEquals(108, step.plannedCount());
        assertEquals(108, step.insertions().stream().mapToLong(RestockInsertion::count).sum());
        assertEquals(108, step.insertions().stream().mapToLong(RestockInsertion::count).sum() + step.currentCount() - 148);
    }

    @Test
    void acceptsPartialSourceAsBestEffort() {
        RestockStep step = onlyStep(plan(List.of(target(TORCH, 64, 64)), inventory(slot(0, TORCH, 10, false)), Map.of(TORCH, 20L)));
        assertEquals(20, step.plannedCount());
    }

    @Test
    void neverPlansMoreThanDestinationCapacity() {
        List<RestockPlayerSlot> slots = inventory(slot(0, COBBLE, 32, false));
        for (int index = 1; index < 36; index++) {
            slots.set(index, slot(index, ItemIdentity.simple("example:full_" + index), 64, false));
        }
        RestockStep step = onlyStep(plan(List.of(target(COBBLE, 256, 64)), slots, Map.of(COBBLE, 1000L)));
        assertEquals(32, step.plannedCount());
        assertTrue(step.plannedCount() <= step.insertions().stream().mapToLong(RestockInsertion::count).sum());
    }

    @Test
    void missingSourceAndDestinationProduceNoExtraction() {
        assertEquals(0, onlyStep(plan(List.of(target(TORCH, 64, 64)), inventory(), Map.of())).plannedCount());
        List<RestockPlayerSlot> full = inventory();
        for (int index = 0; index < 36; index++) {
            full.set(index, slot(index, ItemIdentity.simple("example:occupied_" + index), 64, false));
        }
        assertEquals(0, onlyStep(plan(List.of(target(TORCH, 64, 64)), full, Map.of(TORCH, 64L))).plannedCount());
    }

    @Test
    void protectedStacksCountTowardTargetButAreNeverDestinationSlots() {
        List<RestockPlayerSlot> slots = inventory(slot(0, TORCH, 32, true), slot(1, TORCH, 20, false));
        RestockStep step = onlyStep(plan(List.of(target(TORCH, 64, 64)), slots, Map.of(TORCH, 200L)));
        assertEquals(52, step.currentCount());
        assertEquals(12, step.plannedCount());
        assertTrue(step.insertions().stream().noneMatch(insertion -> insertion.playerSlot() == 0));
    }

    @Test
    void protectedOnlyStackIsNotModifiedEvenWhenItMatchesTarget() {
        List<RestockPlayerSlot> slots = inventory(slot(0, TORCH, 10, true));
        for (int index = 1; index < 36; index++) {
            slots.set(index, slot(index, ItemIdentity.simple("example:occupied_" + index), 64, false));
        }
        RestockStep step = onlyStep(plan(List.of(target(TORCH, 64, 64)), slots, Map.of(TORCH, 500L)));
        assertEquals(0, step.plannedCount());
        assertTrue(step.insertions().isEmpty());
    }

    @Test
    void desiredQuantityCanExceedOneStack() {
        List<RestockPlayerSlot> slots = inventory(
            slot(0, COBBLE, 64, false), slot(1, COBBLE, 64, false), slot(2, COBBLE, 20, false)
        );
        RestockStep step = onlyStep(plan(List.of(target(COBBLE, 256, 64)), slots, Map.of(COBBLE, 108L)));
        assertEquals(108, step.plannedCount());
        assertEquals(List.of(44, 64), step.insertions().stream().map(RestockInsertion::count).toList());
    }

    @Test
    void onlyExactIdentityVariantIsCountedAndPlanned() {
        ItemIdentity enchanted = new ItemIdentity("minecraft:bow", "{enchantments:[power:3]}");
        ItemIdentity plain = new ItemIdentity("minecraft:bow", "");
        RestockStep step = onlyStep(plan(
            List.of(target(enchanted, 1, 1)), inventory(slot(0, plain, 1, false)), Map.of(enchanted, 1L)
        ));
        assertEquals(0, step.currentCount());
        assertEquals(1, step.plannedCount());
    }

    @Test
    void targetsAreIndependentWhenOneIsSatisfiedPartialOrMissing() {
        List<RestockTarget> targets = List.of(
            target(TORCH, 64, 64), target(BREAD, 32, 64), target(ARROW, 64, 64)
        );
        List<RestockPlayerSlot> slots = inventory(slot(0, TORCH, 64, false), slot(1, BREAD, 8, false));
        RestockPlan plan = plan(targets, slots, Map.of(BREAD, 10L));
        assertEquals(List.of(0L, 10L, 0L), plan.steps().stream().map(RestockStep::plannedCount).toList());
        assertEquals(10, plan.plannedItems());
    }

    @Test
    void rejectsInvalidAndDuplicateTargets() {
        assertThrows(IllegalArgumentException.class, () -> target(TORCH, 0, 64));
        RestockTargetBook book = new RestockTargetBook();
        assertEquals(RestockTargetBook.Change.INVALID, book.set(TORCH, -1, 64));
        assertEquals(RestockTargetBook.Change.INVALID, book.set(TORCH, 4097, 64));
        assertEquals(RestockTargetBook.Change.INVALID, book.set(TORCH, 37, 1));
        assertEquals(RestockTargetBook.Change.ADDED, book.set(TORCH, 32, 64));
        assertEquals(RestockTargetBook.Change.UPDATED, book.set(TORCH, 256, 64));
        assertEquals(1, book.entries().size());
        assertEquals(256, book.entries().get(0).desiredCount());
        assertTrue(book.remove(0));
        assertFalse(book.remove(0));
    }

    @Test
    void rejectsTargetCountsBeyondActualInventoryCapacity() {
        RestockTargetBook book = new RestockTargetBook();
        assertEquals(RestockTargetBook.Change.INVALID, book.set(TORCH, 2305, 64));
        assertEquals(RestockTargetBook.Change.INVALID, book.set(ItemIdentity.simple("minecraft:diamond_sword"), 37, 1));
    }

    @Test
    void planConservesItemsAndAccountsForUnprotectedSpaceAcrossTargets() {
        List<RestockPlayerSlot> slots = inventory(slot(0, BREAD, 60, false), slot(1, COBBLE, 63, false));
        RestockPlan plan = plan(
            List.of(target(BREAD, 64, 64), target(COBBLE, 128, 64)), slots, Map.of(BREAD, 20L, COBBLE, 100L)
        );
        assertEquals(List.of(4L, 65L), plan.steps().stream().map(RestockStep::plannedCount).toList());
        assertEquals(69, plan.plannedItems());
        assertEquals(plan.plannedItems(), plan.steps().stream()
            .flatMap(step -> step.insertions().stream())
            .mapToLong(RestockInsertion::count).sum());
        assertTrue(plan.plannedItems() <= 120);
    }

    private static RestockPlan plan(
        List<RestockTarget> targets,
        List<RestockPlayerSlot> slots,
        Map<ItemIdentity, Long> sourceCounts
    ) {
        return RestockPlanner.plan(targets, slots, sourceCounts);
    }

    private static RestockStep onlyStep(RestockPlan plan) {
        return plan.steps().get(0);
    }

    private static RestockTarget target(ItemIdentity identity, int desired, int maxStack) {
        return new RestockTarget(identity, desired, maxStack);
    }

    private static List<RestockPlayerSlot> inventory(RestockPlayerSlot... occupied) {
        Map<Integer, RestockPlayerSlot> bySlot = new HashMap<>();
        for (RestockPlayerSlot slot : occupied) {
            bySlot.put(slot.slot(), slot);
        }
        List<RestockPlayerSlot> result = new ArrayList<>(36);
        for (int index = 0; index < 36; index++) {
            result.add(bySlot.getOrDefault(index, new RestockPlayerSlot(index, null, 0, false)));
        }
        return result;
    }

    private static RestockPlayerSlot slot(int slot, ItemIdentity identity, int count, boolean protectedSlot) {
        return new RestockPlayerSlot(slot, identity, count, protectedSlot);
    }
}
