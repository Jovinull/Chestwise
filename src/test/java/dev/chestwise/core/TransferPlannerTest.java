package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TransferPlannerTest {
    @Test
    void plansDeterministicExactVariantExtractionAcrossSources() {
        ItemDescriptor potion = new ItemDescriptor(
            new ItemIdentity("minecraft:potion", "{potion:minecraft:healing}"), "Potion of Healing", Set.of(), 1);
        IndexedItem indexed = new IndexedItem(potion, 13, List.of(
            new PhysicalSlotRef("z-last", 0, 2, 8),
            new PhysicalSlotRef("a-first", 4, 7, 5)
        ));

        ExtractionPlan plan = TransferPlanner.extract(indexed, 9);

        assertTrue(plan.complete());
        assertEquals(2, plan.steps().size());
        assertEquals("a-first", plan.steps().get(0).slot().sourceId());
        assertEquals(5, plan.steps().get(0).amount());
        assertEquals(4, plan.steps().get(1).amount());
    }

    @Test
    void reportsAPlanAsPartialInsteadOfInventingItems() {
        ItemDescriptor item = new ItemDescriptor(ItemIdentity.simple("minecraft:stone"), "Stone", Set.of(), 1);
        IndexedItem indexed = new IndexedItem(item, 3, List.of(new PhysicalSlotRef("a", 0, 1, 3)));
        ExtractionPlan plan = TransferPlanner.extract(indexed, 64);
        assertFalse(plan.complete());
        assertEquals(3, plan.planned());
    }
}

