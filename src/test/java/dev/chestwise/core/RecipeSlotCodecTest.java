package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeSlotCodecTest {
    @Test
    void roundTripsAGridWithGaps() {
        List<List<String>> grid = new ArrayList<>();
        grid.add(List.of("minecraft:iron_ingot"));
        grid.add(List.of());
        grid.add(List.of("minecraft:oak_planks", "minecraft:birch_planks"));

        List<List<String>> decoded = RecipeSlotCodec.decode(RecipeSlotCodec.encode(grid));

        assertEquals(RecipeSlotCodec.SLOTS, decoded.size());
        assertEquals(List.of("minecraft:iron_ingot"), decoded.get(0));
        assertEquals(List.of(), decoded.get(1));
        assertEquals(List.of("minecraft:oak_planks", "minecraft:birch_planks"), decoded.get(2));
        for (int slot = 3; slot < RecipeSlotCodec.SLOTS; slot++) {
            assertTrue(decoded.get(slot).isEmpty(), "slot " + slot + " should be padded empty");
        }
    }

    @Test
    void alwaysProducesExactlyOneGroupPerGridSlot() {
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.decode("").size());
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.decode("a").size());
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.encode(List.of()).split(";", -1).length);
    }

    @Test
    void capsAlternativesSoOneTagCannotFloodThePayload() {
        List<String> many = new ArrayList<>();
        for (int index = 0; index < RecipeSlotCodec.MAX_ALTERNATIVES * 3; index++) {
            many.add("mod:item_" + index);
        }
        List<List<String>> decoded = RecipeSlotCodec.decode(RecipeSlotCodec.encode(List.of(many)));
        assertEquals(RecipeSlotCodec.MAX_ALTERNATIVES, decoded.get(0).size());
    }

    @Test
    void dropsBlanksAndDuplicatesRatherThanEmittingEmptyIds() {
        List<List<String>> grid = List.of(List.of("  minecraft:stone  ", "", "minecraft:stone", "   "));
        assertEquals(List.of("minecraft:stone"), RecipeSlotCodec.decode(RecipeSlotCodec.encode(grid)).get(0));
    }

    @Test
    void survivesMalformedPayloadsInsteadOfThrowing() {
        // These arrive over the network, so a bad payload must degrade quietly.
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.decode(";;;;;;;;").size());
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.decode(",,,;,,,").size());
        assertEquals(RecipeSlotCodec.SLOTS, RecipeSlotCodec.decode(null).size());
        assertTrue(RecipeSlotCodec.decode(",,,").get(0).isEmpty());
    }

    @Test
    void ignoresGroupsBeyondTheGridInsteadOfShiftingSlots() {
        List<List<String>> tooMany = new ArrayList<>();
        for (int slot = 0; slot < RecipeSlotCodec.SLOTS + 4; slot++) {
            tooMany.add(List.of("mod:slot_" + slot));
        }
        List<List<String>> decoded = RecipeSlotCodec.decode(RecipeSlotCodec.encode(tooMany));
        assertEquals(RecipeSlotCodec.SLOTS, decoded.size());
        assertEquals(List.of("mod:slot_0"), decoded.get(0));
        assertEquals(List.of("mod:slot_8"), decoded.get(RecipeSlotCodec.SLOTS - 1));
    }
}
