package com.sn.smartpantry.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One required ingredient line for a Recipe, e.g. "recipeId=3 needs 2 cup of rice".
 * This is what the strict-matching algorithm checks against the user's PantryItems.
 */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("recipe_id")}
)
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "recipe_id")
    public int recipeId;

    @NonNull
    @ColumnInfo(name = "ingredient_name")
    public String ingredientName;

    @ColumnInfo(name = "required_quantity")
    public double requiredQuantity;

    @ColumnInfo(name = "unit")
    public String unit;

    public RecipeIngredient(int recipeId, @NonNull String ingredientName,
                             double requiredQuantity, String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }
}
