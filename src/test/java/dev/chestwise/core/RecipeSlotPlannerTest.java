package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecipeSlotPlannerTest {
    @Test
    void rejectsAnIncompleteRecipeWithoutProducingAPartialAssignment() {
        List<List<String>> ingredients = List.of(
            List.of("minecraft:diamond"),
            List.of("minecraft:diamond"),
            List.of("minecraft:diamond")
        );

        assertTrue(RecipeSlotPlanner.plan(ingredients, Map.of("minecraft:diamond", 2L)).isEmpty());
    }

    @Test
    void findsACompleteAssignmentWhenIngredientAlternativesOverlap() {
        List<List<String>> ingredients = List.of(
            List.of("minecraft:diamond", "minecraft:emerald"),
            List.of("minecraft:diamond")
        );

        assertEquals(
            List.of("minecraft:emerald", "minecraft:diamond"),
            RecipeSlotPlanner.plan(ingredients, Map.of(
                "minecraft:diamond", 1L,
                "minecraft:emerald", 1L
            )).orElseThrow()
        );
    }

    @Test
    void prefersAnInventoryAlternativeOverOneOnlyAvailableInStorage() {
        List<List<String>> ingredients = List.of(List.of("minecraft:diamond", "minecraft:emerald"));

        assertEquals(
            List.of("minecraft:emerald"),
            RecipeSlotPlanner.plan(ingredients,
                Map.of("minecraft:diamond", 1L, "minecraft:emerald", 1L),
                Map.of("minecraft:emerald", 1L)
            ).orElseThrow()
        );
    }

    @Test
    void preservesEmptyGridSlotsInTheAssignment() {
        List<List<String>> ingredients = List.of(List.of(), List.of("minecraft:stick"), List.of());

        assertEquals(
            List.of("", "minecraft:stick", ""),
            RecipeSlotPlanner.plan(ingredients, Map.of("minecraft:stick", 1L)).orElseThrow()
        );
    }

    @Test
    void rejectsAnEmptyRecipeInsteadOfTreatingItAsACompleteTransfer() {
        assertTrue(RecipeSlotPlanner.plan(List.of(List.of(), List.of()), Map.of()).isEmpty());
    }
}
