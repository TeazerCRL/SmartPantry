package com.sn.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.sn.smartpantry.data.RecipeMatcher;
import com.sn.smartpantry.data.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the strict matcher against the pantry and shows the results.
 * "Ready to cook" lists only recipes with NOTHING missing (the strict rule).
 * "Almost there" is kept on a separate tab and lists recipes missing exactly one.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private RecipeRepository repository;
    private RecipeAdapter adapter;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton readyButton, almostButton;
    private TextView emptyText;

    private List<RecipeMatcher.RecipeMatch> readyToCook = new ArrayList<>();
    private List<RecipeMatcher.RecipeMatch> almostThere = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.suggestedRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new RecipeRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        toggleGroup = findViewById(R.id.toggleGroup);
        readyButton = findViewById(R.id.readyButton);
        almostButton = findViewById(R.id.almostButton);
        emptyText = findViewById(R.id.emptyText);

        RecyclerView recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recyclerView.setAdapter(adapter);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showSelectedList();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-run the matcher every time the screen comes back into view,
        // so pantry changes made elsewhere are always reflected.
        loadMatches();
    }

    private void loadMatches() {
        repository.loadMatches(results -> {
            // Still on the background thread here: split the results first.
            List<RecipeMatcher.RecipeMatch> ready = new ArrayList<>();
            List<RecipeMatcher.RecipeMatch> almost = new ArrayList<>();
            for (RecipeMatcher.RecipeMatch match : results) {
                if (match.canMakeNow()) {
                    ready.add(match);
                } else if (match.missing.size() == 1) {
                    almost.add(match);
                }
            }
            // Views can only be touched on the main (UI) thread.
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                readyToCook = ready;
                almostThere = almost;
                readyButton.setText(getString(R.string.tab_ready, ready.size()));
                almostButton.setText(getString(R.string.tab_almost, almost.size()));
                showSelectedList();
            });
        });
    }

    private void showSelectedList() {
        boolean showReady = toggleGroup.getCheckedButtonId() != R.id.almostButton;
        List<RecipeMatcher.RecipeMatch> list = showReady ? readyToCook : almostThere;

        adapter.submitList(list, !showReady);
        emptyText.setText(showReady ? R.string.empty_ready : R.string.empty_almost);
        emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClicked(RecipeMatcher.RecipeMatch match) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, match.recipe.id);
        startActivity(intent);
    }
}
