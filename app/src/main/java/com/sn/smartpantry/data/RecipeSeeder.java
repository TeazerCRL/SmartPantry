package com.sn.smartpantry.data;

import com.sn.smartpantry.data.dao.RecipeDao;
import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * One-time seed data: 18 simple recipes with realistic ingredient lists.
 * Called from AppDatabase's onCreate callback so it only ever runs once,
 * on first app install.
 *
 * Feel free to add/edit recipes here — this is the easiest place to tune
 * your "at least 15-20 recipes" requirement and to add ingredients that
 * exercise the strict-matching rule in your video demo.
 */
public class RecipeSeeder {

    public static void seed(RecipeDao recipeDao) {
        addRecipe(recipeDao, "Tomato Egg Stir Fry",
                "1. Beat the eggs.\n2. Stir-fry tomato until soft.\n3. Add eggs, scramble together.\n4. Season and serve.",
                ing("egg", 3, "unit"),
                ing("tomato", 2, "unit"),
                ing("salt", 1, "tsp"));

        addRecipe(recipeDao, "Garlic Butter Pasta",
                "1. Boil pasta until al dente.\n2. Melt butter, fry garlic until fragrant.\n3. Toss pasta in garlic butter.\n4. Serve with black pepper.",
                ing("pasta", 200, "g"),
                ing("butter", 30, "g"),
                ing("garlic", 3, "clove"));

        addRecipe(recipeDao, "Chicken Rice Bowl",
                "1. Cook rice.\n2. Pan-fry chicken until cooked through.\n3. Slice chicken over rice.\n4. Drizzle soy sauce.",
                ing("rice", 1, "cup"),
                ing("chicken breast", 1, "unit"),
                ing("soy sauce", 1, "tbsp"));

        addRecipe(recipeDao, "Simple Omelette",
                "1. Beat eggs with a splash of milk.\n2. Pour into a hot buttered pan.\n3. Fold once set.\n4. Serve warm.",
                ing("egg", 2, "unit"),
                ing("milk", 2, "tbsp"),
                ing("butter", 10, "g"));

        addRecipe(recipeDao, "Cheese Toast",
                "1. Toast the bread.\n2. Melt cheese on top.\n3. Serve hot.",
                ing("bread", 2, "slice"),
                ing("cheese", 2, "slice"));

        addRecipe(recipeDao, "Vegetable Soup",
                "1. Boil water or stock.\n2. Add chopped vegetables.\n3. Simmer 15 minutes.\n4. Season to taste.",
                ing("carrot", 2, "unit"),
                ing("potato", 2, "unit"),
                ing("onion", 1, "unit"),
                ing("salt", 1, "tsp"));

        addRecipe(recipeDao, "Banana Pancakes",
                "1. Mash banana.\n2. Mix with flour, egg and milk into a batter.\n3. Fry spoonfuls until golden on both sides.",
                ing("banana", 1, "unit"),
                ing("flour", 1, "cup"),
                ing("egg", 1, "unit"),
                ing("milk", 0.5, "cup"));

        addRecipe(recipeDao, "Fried Rice",
                "1. Fry leftover rice in oil.\n2. Add egg, scramble through.\n3. Add vegetables and soy sauce.\n4. Stir-fry until heated through.",
                ing("rice", 2, "cup"),
                ing("egg", 1, "unit"),
                ing("soy sauce", 1, "tbsp"),
                ing("carrot", 1, "unit"));

        addRecipe(recipeDao, "Grilled Cheese Sandwich",
                "1. Butter the outside of two bread slices.\n2. Place cheese between them.\n3. Grill both sides until golden and melted.",
                ing("bread", 2, "slice"),
                ing("cheese", 2, "slice"),
                ing("butter", 10, "g"));

        addRecipe(recipeDao, "Tuna Salad",
                "1. Drain the tuna.\n2. Mix with mayonnaise and chopped onion.\n3. Serve on its own or with bread.",
                ing("tuna", 1, "can"),
                ing("mayonnaise", 2, "tbsp"),
                ing("onion", 0.5, "unit"));

        addRecipe(recipeDao, "Potato Wedges",
                "1. Cut potatoes into wedges.\n2. Toss in oil and salt.\n3. Bake at 200C for 25 minutes, turning once.",
                ing("potato", 4, "unit"),
                ing("oil", 2, "tbsp"),
                ing("salt", 1, "tsp"));

        addRecipe(recipeDao, "Chicken Noodle Soup",
                "1. Boil stock.\n2. Add shredded chicken and noodles.\n3. Simmer until noodles are cooked.\n4. Season and serve.",
                ing("chicken breast", 1, "unit"),
                ing("noodles", 100, "g"),
                ing("carrot", 1, "unit"));

        addRecipe(recipeDao, "Rice and Beans",
                "1. Cook rice.\n2. Heat beans with onion and garlic.\n3. Serve beans over rice.",
                ing("rice", 1, "cup"),
                ing("beans", 1, "cup"),
                ing("onion", 1, "unit"),
                ing("garlic", 1, "clove"));

        addRecipe(recipeDao, "Egg Fried Noodles",
                "1. Boil noodles.\n2. Scramble egg in a hot pan.\n3. Add noodles and soy sauce, toss together.",
                ing("noodles", 150, "g"),
                ing("egg", 2, "unit"),
                ing("soy sauce", 1, "tbsp"));

        addRecipe(recipeDao, "Mashed Potato",
                "1. Boil potatoes until soft.\n2. Mash with butter and milk.\n3. Season with salt.",
                ing("potato", 3, "unit"),
                ing("butter", 20, "g"),
                ing("milk", 0.25, "cup"),
                ing("salt", 1, "tsp"));

        addRecipe(recipeDao, "Carrot and Onion Stir Fry",
                "1. Slice carrot and onion thinly.\n2. Stir-fry in oil until soft.\n3. Season with salt and serve.",
                ing("carrot", 2, "unit"),
                ing("onion", 1, "unit"),
                ing("oil", 1, "tbsp"),
                ing("salt", 0.5, "tsp"));

        addRecipe(recipeDao, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread peanut butter.\n3. Top with sliced banana.",
                ing("bread", 2, "slice"),
                ing("peanut butter", 2, "tbsp"),
                ing("banana", 1, "unit"));

        addRecipe(recipeDao, "Simple Tomato Pasta",
                "1. Boil pasta.\n2. Cook chopped tomato with garlic until saucy.\n3. Toss pasta through the sauce.",
                ing("pasta", 200, "g"),
                ing("tomato", 3, "unit"),
                ing("garlic", 2, "clove"));
    }

    private static void addRecipe(RecipeDao dao, String name, String instructions,
                                   RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(name, instructions);
        long recipeId = dao.insertRecipe(recipe);

        List<RecipeIngredient> toInsert = new ArrayList<>();
        for (RecipeIngredient ri : ingredients) {
            ri.recipeId = (int) recipeId;
            toInsert.add(ri);
        }
        dao.insertIngredients(toInsert);
    }

    // Convenience: recipeId is set to 0 here and overwritten in addRecipe() above.
    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(0, name, quantity, unit);
    }
}
