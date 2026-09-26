package com.sn.smartpantry.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.sn.smartpantry.data.dao.PantryDao;
import com.sn.smartpantry.data.dao.RecipeDao;
import com.sn.smartpantry.data.entity.PantryItem;
import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives the recipe screens access to recipe data and the matching logic,
 * so no Activity talks to Room directly. Anything that reads the database
 * outside of LiveData runs on the background executor, and the result is
 * handed back through a Callback.
 */
public class RecipeRepository {

    /** Receives a result produced on a background thread. */
    public interface Callback<T> {
        void onResult(T result);
    }

    private final RecipeDao recipeDao;
    private final PantryDao pantryDao;

    public RecipeRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        recipeDao = db.recipeDao();
        pantryDao = db.pantryDao();
    }

    /**
     * Loads all recipes, their ingredients and the current pantry, then runs
     * the strict matcher. The callback is called on a background thread.
     */
    public void loadMatches(Callback<List<RecipeMatcher.RecipeMatch>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Recipe> recipes = recipeDao.getAllRecipesSnapshot();
            Map<Integer, List<RecipeIngredient>> ingredientsByRecipe = new HashMap<>();
            for (Recipe recipe : recipes) {
                ingredientsByRecipe.put(recipe.id, recipeDao.getIngredientsForRecipe(recipe.id));
            }
            List<PantryItem> pantry = pantryDao.getAllItemsSnapshot();
            callback.onResult(RecipeMatcher.matchAll(recipes, ingredientsByRecipe, pantry));
        });
    }

    public LiveData<Recipe> getRecipeById(int recipeId) {
        return recipeDao.getRecipeById(recipeId);
    }

    /** Loads one recipe's ingredient list. The callback is called on a background thread. */
    public void loadIngredients(int recipeId, Callback<List<RecipeIngredient>> callback) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                callback.onResult(recipeDao.getIngredientsForRecipe(recipeId)));
    }
}
