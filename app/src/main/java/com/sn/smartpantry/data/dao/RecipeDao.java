package com.sn.smartpantry.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sn.smartpantry.data.entity.Recipe;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    long insertRecipe(Recipe recipe);

    @Insert
    void insertIngredients(List<RecipeIngredient> ingredients);

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    LiveData<List<Recipe>> getAllRecipesLive();

    // Snapshot version for the matching algorithm (runs off the main thread).
    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipesSnapshot();

    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    List<RecipeIngredient> getIngredientsForRecipe(int recipeId);

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    LiveData<Recipe> getRecipeById(int recipeId);

    @Query("SELECT COUNT(*) FROM recipes")
    int countRecipes();
}
