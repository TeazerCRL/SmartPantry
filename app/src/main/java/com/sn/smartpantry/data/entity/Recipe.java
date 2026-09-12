package com.sn.smartpantry.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A recipe the user could cook. The actual required ingredients live in
 * RecipeIngredient (one-to-many), not here, so a recipe can need any number
 * of ingredients.
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    // Preparation steps, stored as one block of text (numbered steps separated by \n).
    @NonNull
    @ColumnInfo(name = "instructions")
    public String instructions;

    public Recipe(@NonNull String name, @NonNull String instructions) {
        this.name = name;
        this.instructions = instructions;
    }
}
