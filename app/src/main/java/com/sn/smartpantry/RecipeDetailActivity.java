package com.sn.smartpantry;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.sn.smartpantry.data.RecipeRepository;
import com.sn.smartpantry.data.entity.RecipeIngredient;

import java.util.List;

/** Shows one recipe's full ingredient list and method. Needs EXTRA_RECIPE_ID. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final int NO_ID = -1;

    private TextView ingredientsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ingredientsText = findViewById(R.id.ingredientsText);
        TextView instructionsText = findViewById(R.id.instructionsText);

        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, NO_ID);
        if (recipeId == NO_ID) {
            finish(); // opened without a recipe, nothing to show
            return;
        }

        RecipeRepository repository = new RecipeRepository(getApplication());

        repository.getRecipeById(recipeId).observe(this, recipe -> {
            if (recipe == null) {
                Toast.makeText(this, R.string.msg_recipe_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(recipe.name);
            instructionsText.setText(recipe.instructions);
        });

        repository.loadIngredients(recipeId, ingredients -> runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                ingredientsText.setText(formatIngredients(ingredients));
            }
        }));
    }

    /** Builds lines like "• 200 g pasta" or "• 3 egg" (the word "unit" is left out). */
    private static String formatIngredients(List<RecipeIngredient> ingredients) {
        StringBuilder text = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append("• ").append(formatQuantity(ingredient.requiredQuantity)).append(' ');
            if (ingredient.unit != null && !ingredient.unit.equals("unit")) {
                text.append(ingredient.unit).append(' ');
            }
            text.append(ingredient.ingredientName);
        }
        return text.toString();
    }

    private static String formatQuantity(double quantity) {
        return quantity == Math.floor(quantity)
                ? String.valueOf((int) quantity)
                : String.valueOf(quantity);
    }
}
