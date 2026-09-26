package com.sn.smartpantry;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sn.smartpantry.data.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/** Shows a list of matched recipes as cards on the Suggested Recipes screen. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClicked(RecipeMatcher.RecipeMatch match);
    }

    private List<RecipeMatcher.RecipeMatch> matches = new ArrayList<>();
    private boolean showMissing = false;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    /**
     * @param newMatches  the recipes to show
     * @param showMissing true for the "Almost there" list, so each card says what's missing
     */
    public void submitList(List<RecipeMatcher.RecipeMatch> newMatches, boolean showMissing) {
        this.matches = newMatches != null ? newMatches : new ArrayList<>();
        this.showMissing = showMissing;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeMatcher.RecipeMatch match = matches.get(position);
        holder.name.setText(match.recipe.name);

        if (showMissing) {
            holder.details.setText(holder.itemView.getContext().getString(
                    R.string.recipe_missing, TextUtils.join(", ", match.missing)));
            holder.badge.setText(R.string.badge_missing);
            holder.badge.setBackgroundResource(R.drawable.bg_badge_missing);
            holder.badge.setTextColor(holder.missingBadgeColor);
        } else {
            holder.details.setText(holder.itemView.getContext().getString(
                    R.string.recipe_ingredient_count, match.ingredients.size()));
            holder.badge.setText(R.string.badge_ready);
            holder.badge.setBackgroundResource(R.drawable.bg_badge_ready);
            holder.badge.setTextColor(holder.readyBadgeColor);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(match));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView name, details, badge;
        final int readyBadgeColor, missingBadgeColor;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeName);
            details = itemView.findViewById(R.id.recipeDetails);
            badge = itemView.findViewById(R.id.recipeBadge);
            // Text colours that match the green and orange badge backgrounds.
            readyBadgeColor = ContextCompat.getColor(itemView.getContext(),
                    R.color.sp_on_primary_container);
            missingBadgeColor = ContextCompat.getColor(itemView.getContext(),
                    R.color.sp_on_secondary_container);
        }
    }
}
