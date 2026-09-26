package com.sn.smartpantry.data;

import com.sn.smartpantry.data.entity.PantryItem;
import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The strict-matching rule: a recipe can be cooked "now" only if EVERY
 * ingredient it needs is in the pantry in at least the required amount.
 *
 * Before comparing, names and units are normalised so small real-world
 * differences don't break the match:
 *  - Names: lower case, extra spaces removed, last word made singular
 *    ("Tomatoes" -> "tomato", "Chicken Breasts" -> "chicken breast"), and a
 *    few common synonyms mapped ("mayo" -> "mayonnaise", "soya sauce" -> "soy sauce").
 *    The same function is applied to pantry names AND recipe names, so both
 *    sides always end up in the same form.
 *  - Units: converted to one base unit per type (mass -> grams, volume -> ml),
 *    so 1 kg of rice in the pantry covers a recipe needing 200 g.
 *    Count units (unit, clove, slice, can) can't be converted into each other,
 *    so they must match exactly.
 *
 * If two amounts can't be compared (e.g. grams vs cups), the ingredient counts
 * as missing. The rule is strict, so the matcher never guesses.
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-6; // tolerance for decimal rounding

    private static final String TYPE_MASS = "mass";
    private static final String TYPE_VOLUME = "volume";

    private static final Map<String, Double> GRAMS_PER_UNIT = new HashMap<>();
    private static final Map<String, Double> ML_PER_UNIT = new HashMap<>();
    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    private static final Map<String, String> NAME_ALIASES = new HashMap<>();

    static {
        GRAMS_PER_UNIT.put("g", 1.0);
        GRAMS_PER_UNIT.put("kg", 1000.0);

        ML_PER_UNIT.put("ml", 1.0);
        ML_PER_UNIT.put("l", 1000.0);
        ML_PER_UNIT.put("tsp", 5.0);
        ML_PER_UNIT.put("tbsp", 15.0);
        ML_PER_UNIT.put("cup", 250.0); // metric cup

        String[][] units = {
                {"gram", "g"}, {"grams", "g"}, {"gr", "g"},
                {"kilogram", "kg"}, {"kilograms", "kg"}, {"kgs", "kg"},
                {"millilitre", "ml"}, {"millilitres", "ml"}, {"milliliter", "ml"}, {"milliliters", "ml"},
                {"litre", "l"}, {"litres", "l"}, {"liter", "l"}, {"liters", "l"}, {"ltr", "l"},
                {"teaspoon", "tsp"}, {"teaspoons", "tsp"},
                {"tablespoon", "tbsp"}, {"tablespoons", "tbsp"},
                {"cups", "cup"},
                {"units", "unit"}, {"piece", "unit"}, {"pieces", "unit"}, {"pcs", "unit"}, {"whole", "unit"},
                {"cloves", "clove"}, {"slices", "slice"},
                {"cans", "can"}, {"tin", "can"}, {"tins", "can"}
        };
        for (String[] pair : units) {
            UNIT_ALIASES.put(pair[0], pair[1]);
        }

        // Keys must already be in normalised form (lower case, singular last word).
        String[][] names = {
                {"mayo", "mayonnaise"},
                {"soya sauce", "soy sauce"},
                {"spaghetti", "pasta"}, {"macaroni", "pasta"}, {"penne", "pasta"},
                {"cheddar", "cheese"}, {"cheddar cheese", "cheese"},
                {"chicken", "chicken breast"}, {"chicken fillet", "chicken breast"},
                {"cooking oil", "oil"}, {"vegetable oil", "oil"}, {"sunflower oil", "oil"}, {"olive oil", "oil"},
                {"white rice", "rice"}, {"brown rice", "rice"},
                {"white bread", "bread"}, {"brown bread", "bread"},
                {"garlic clove", "garlic"},
                {"egg noodle", "noodle"},
                {"baked bean", "bean"},
                {"tinned tuna", "tuna"}, {"canned tuna", "tuna"},
                {"table salt", "salt"}
        };
        for (String[] pair : names) {
            NAME_ALIASES.put(pair[0], pair[1]);
        }
    }

    private RecipeMatcher() {
        // Utility class: only static methods.
    }

    /** The result of checking one recipe against the pantry. */
    public static class RecipeMatch {
        public final Recipe recipe;
        public final List<RecipeIngredient> ingredients;
        public final List<String> missing; // names of ingredients not covered

        RecipeMatch(Recipe recipe, List<RecipeIngredient> ingredients, List<String> missing) {
            this.recipe = recipe;
            this.ingredients = ingredients;
            this.missing = missing;
        }

        /** True only when nothing at all is missing: the strict rule. */
        public boolean canMakeNow() {
            return missing.isEmpty();
        }
    }

    /** An amount converted to a base unit, e.g. 1.5 kg becomes (mass, 1500). */
    static final class Amount {
        final String type;
        final double value;

        Amount(String type, double value) {
            this.type = type;
            this.value = value;
        }
    }

    /**
     * Checks every recipe against the pantry.
     *
     * @param recipes             all recipes
     * @param ingredientsByRecipe each recipe's required ingredients, keyed by recipe id
     * @param pantry              everything the user currently has
     * @return one RecipeMatch per recipe, sorted by recipe name
     */
    public static List<RecipeMatch> matchAll(List<Recipe> recipes,
                                             Map<Integer, List<RecipeIngredient>> ingredientsByRecipe,
                                             List<PantryItem> pantry) {
        Map<String, Map<String, Double>> stock = buildStock(pantry);
        List<RecipeMatch> results = new ArrayList<>();

        for (Recipe recipe : recipes) {
            List<RecipeIngredient> needed = ingredientsByRecipe.get(recipe.id);
            if (needed == null || needed.isEmpty()) {
                continue; // a recipe with no ingredient list can't be judged, so skip it
            }
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient ingredient : needed) {
                if (!hasEnough(stock, ingredient)) {
                    missing.add(ingredient.ingredientName);
                }
            }
            results.add(new RecipeMatch(recipe, needed, missing));
        }

        Collections.sort(results, (a, b) -> a.recipe.name.compareToIgnoreCase(b.recipe.name));
        return results;
    }

    /**
     * Totals the pantry by normalised name and unit type, so two separate
     * entries like "Egg 2" and "eggs 4" count as 6 eggs.
     */
    static Map<String, Map<String, Double>> buildStock(List<PantryItem> pantry) {
        Map<String, Map<String, Double>> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = normaliseName(item.name);
            Amount amount = toBaseAmount(item.quantity, item.unit);

            Map<String, Double> byType = stock.get(key);
            if (byType == null) {
                byType = new HashMap<>();
                stock.put(key, byType);
            }
            Double current = byType.get(amount.type);
            byType.put(amount.type, (current == null ? 0 : current) + amount.value);
        }
        return stock;
    }

    /** True if the pantry holds at least the required amount of this ingredient. */
    static boolean hasEnough(Map<String, Map<String, Double>> stock, RecipeIngredient ingredient) {
        Map<String, Double> byType = stock.get(normaliseName(ingredient.ingredientName));
        if (byType == null) {
            return false; // not in the pantry at all
        }
        Amount required = toBaseAmount(ingredient.requiredQuantity, ingredient.unit);
        Double available = byType.get(required.type);
        return available != null && available + EPSILON >= required.value;
    }

    /** Converts a quantity to its base unit: grams for mass, ml for volume. */
    static Amount toBaseAmount(double quantity, String rawUnit) {
        String unit = normaliseUnit(rawUnit);
        Double grams = GRAMS_PER_UNIT.get(unit);
        if (grams != null) {
            return new Amount(TYPE_MASS, quantity * grams);
        }
        Double ml = ML_PER_UNIT.get(unit);
        if (ml != null) {
            return new Amount(TYPE_VOLUME, quantity * ml);
        }
        // Count units only compare with themselves: "count:clove" vs "count:clove".
        return new Amount("count:" + unit, quantity);
    }

    static String normaliseUnit(String rawUnit) {
        if (rawUnit == null) {
            return "unit";
        }
        String unit = rawUnit.toLowerCase(Locale.ROOT).trim().replace(".", "");
        if (unit.isEmpty()) {
            return "unit";
        }
        String alias = UNIT_ALIASES.get(unit);
        return alias != null ? alias : unit;
    }

    /** "  Cherry TOMATOES " becomes "cherry tomato". */
    public static String normaliseName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (name.isEmpty()) {
            return name;
        }
        int lastSpace = name.lastIndexOf(' ');
        String start = name.substring(0, lastSpace + 1);
        String lastWord = name.substring(lastSpace + 1);
        name = start + toSingular(lastWord);

        String alias = NAME_ALIASES.get(name);
        return alias != null ? alias : name;
    }

    /** Simple English plural rules; good enough for ingredient names. */
    static String toSingular(String word) {
        if (word.length() <= 3) {
            return word; // "gas", "pea", etc.
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";   // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);         // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);         // peaches -> peach
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);         // eggs -> egg
        }
        return word;
    }
}
