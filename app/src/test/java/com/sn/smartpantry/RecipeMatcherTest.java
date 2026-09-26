package com.sn.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sn.smartpantry.data.RecipeMatcher;
import com.sn.smartpantry.data.entity.PantryItem;
import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Unit tests for the strict-matching rule. Run with right-click → Run 'RecipeMatcherTest'. */
public class RecipeMatcherTest {

    private static final List<RecipeIngredient> TOMATO_EGG = Arrays.asList(
            new RecipeIngredient(1, "egg", 3, "unit"),
            new RecipeIngredient(1, "tomato", 2, "unit"),
            new RecipeIngredient(1, "salt", 1, "tsp"));

    private static RecipeMatcher.RecipeMatch check(List<RecipeIngredient> needed, PantryItem... pantry) {
        Recipe recipe = new Recipe("Test recipe", "Steps");
        recipe.id = 1;
        Map<Integer, List<RecipeIngredient>> byRecipe = new HashMap<>();
        byRecipe.put(1, needed);
        return RecipeMatcher.matchAll(Collections.singletonList(recipe), byRecipe,
                Arrays.asList(pantry)).get(0);
    }

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        assertTrue(check(TOMATO_EGG, item("egg", 3, "unit"), item("tomato", 2, "unit"),
                item("salt", 1, "tsp")).canMakeNow());
    }

    @Test
    public void oneIngredientMissing_isNotSuggested() {
        RecipeMatcher.RecipeMatch match = check(TOMATO_EGG, item("egg", 3, "unit"), item("tomato", 2, "unit"));
        assertFalse(match.canMakeNow());
        assertEquals(1, match.missing.size());
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        assertFalse(check(TOMATO_EGG, item("egg", 2, "unit"), item("tomato", 2, "unit"),
                item("salt", 1, "tsp")).canMakeNow());
    }

    @Test
    public void pluralsCapitalsAndSpaces_stillMatch() {
        assertTrue(check(TOMATO_EGG, item("  Eggs ", 6, "unit"), item("Tomatoes", 2, "unit"),
                item("Salt", 5, "ml")).canMakeNow());
    }

    @Test
    public void duplicatePantryEntries_areAddedTogether() {
        assertTrue(check(TOMATO_EGG, item("egg", 1, "unit"), item("eggs", 2, "unit"),
                item("tomato", 2, "unit"), item("salt", 1, "tsp")).canMakeNow());
    }

    @Test
    public void kilogramsCoverGrams() {
        assertTrue(check(Collections.singletonList(new RecipeIngredient(1, "pasta", 200, "g")),
                item("pasta", 0.5, "kg")).canMakeNow());
    }

    @Test
    public void unitsThatCannotBeCompared_countAsMissing() {
        assertFalse(check(Collections.singletonList(new RecipeIngredient(1, "pasta", 200, "g")),
                item("pasta", 2, "cup")).canMakeNow());
    }
}
