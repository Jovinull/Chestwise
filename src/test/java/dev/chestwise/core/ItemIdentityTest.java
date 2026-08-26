package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ItemIdentityTest {
    @Test
    void distinguishesComponentAndDurabilityVariants() {
        ItemIdentity pristine = new ItemIdentity("minecraft:diamond_sword", "{damage:0}");
        ItemIdentity used = new ItemIdentity("minecraft:diamond_sword", "{damage:12}");

        assertNotEquals(pristine, used);
        assertEquals("minecraft", used.namespace());
        assertEquals("diamond_sword", used.path());
    }

    @Test
    void opaqueExactDataDoesNotTrustItsDisplayString() {
        SameTextData firstData = new SameTextData(1);
        SameTextData secondData = new SameTextData(2);
        ItemIdentity first = ItemIdentity.exact("example:component_item", firstData, firstData.toString());
        ItemIdentity second = ItemIdentity.exact("example:component_item", secondData, secondData.toString());

        assertNotEquals(first, second);
        assertNotEquals(0, first.compareTo(second));
    }

    @Test
    void rejectsUnnamespacedIds() {
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.simple("diamond"));
    }

    private record SameTextData(int value) {
        @Override
        public String toString() {
            return "same-text";
        }
    }
}
