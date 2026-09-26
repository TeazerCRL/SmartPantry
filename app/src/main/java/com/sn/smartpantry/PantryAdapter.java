package com.sn.smartpantry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sn.smartpantry.data.entity.PantryItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Shows the pantry items on the Pantry List screen. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemActionListener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;

    private List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;

    // Expiry settings, set from MainActivity using the values on the Settings screen.
    private boolean highlightExpiring = true;
    private int expiringDays = AppSettings.DEFAULT_EXPIRY_DAYS;

    public PantryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PantryItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setExpirySettings(boolean highlight, int days) {
        this.highlightExpiring = highlight;
        this.expiringDays = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.name);
        String qty = (item.quantity == Math.floor(item.quantity))
                ? String.valueOf((int) item.quantity)
                : String.valueOf(item.quantity);
        String unit = item.unit != null ? item.unit : "";
        holder.quantity.setText(qty + " " + unit);

        bindExpiry(holder, item, context);

        holder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(item));
        holder.deleteButton.setContentDescription(
                context.getString(R.string.action_delete_item, item.name));
    }

    /** Shows "Expires in 2 days" etc., in red when the item is expiring soon. */
    private void bindExpiry(PantryViewHolder holder, PantryItem item, Context context) {
        if (item.expiryDate == null) {
            holder.expiry.setVisibility(View.GONE);
            return;
        }
        holder.expiry.setVisibility(View.VISIBLE);

        long daysLeft = daysUntil(item.expiryDate);
        if (daysLeft < 0) {
            holder.expiry.setText(R.string.expiry_expired);
        } else if (daysLeft == 0) {
            holder.expiry.setText(R.string.expiry_today);
        } else {
            holder.expiry.setText(context.getResources().getQuantityString(
                    R.plurals.expiry_in_days, (int) daysLeft, (int) daysLeft));
        }

        boolean warn = highlightExpiring && daysLeft <= expiringDays;
        if (warn) {
            holder.expiry.setTextColor(ContextCompat.getColor(context, R.color.expiry_warning));
            holder.expiry.setTypeface(null, Typeface.BOLD);
        } else {
            holder.expiry.setTextColor(holder.defaultExpiryColor);
            holder.expiry.setTypeface(null, Typeface.NORMAL);
        }
    }

    /** Whole days from today until the given date: 0 = today, negative = already past. */
    static long daysUntil(long expiryMillis) {
        Calendar today = startOfDay(Calendar.getInstance());
        Calendar expiry = Calendar.getInstance();
        expiry.setTimeInMillis(expiryMillis);
        startOfDay(expiry);
        return Math.round((expiry.getTimeInMillis() - today.getTimeInMillis())
                / (double) MILLIS_PER_DAY);
    }

    private static Calendar startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView name, quantity, expiry;
        final ImageButton deleteButton;
        final ColorStateList defaultExpiryColor; // remembered so we can undo the red

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            quantity = itemView.findViewById(R.id.itemQuantity);
            expiry = itemView.findViewById(R.id.itemExpiry);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            defaultExpiryColor = expiry.getTextColors();
        }
    }
}
