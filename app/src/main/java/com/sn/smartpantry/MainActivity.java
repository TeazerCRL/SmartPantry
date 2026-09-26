package com.sn.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sn.smartpantry.data.PantryRepository;
import com.sn.smartpantry.data.entity.PantryItem;

import java.util.ArrayList;
import java.util.List;

/** Home screen: the Pantry List. */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private PantryRepository repository;
    private PantryAdapter adapter;
    private TextView expiryBanner;
    private List<PantryItem> currentItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar menu = the app's navigation element.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_suggestions) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        repository = new PantryRepository(getApplication());

        RecyclerView recyclerView = findViewById(R.id.pantryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recyclerView.setAdapter(adapter);

        View emptyText = findViewById(R.id.emptyText);
        expiryBanner = findViewById(R.id.expiryBanner);

        // LiveData: the list refreshes by itself whenever the pantry table changes.
        repository.getAllItems().observe(this, items -> {
            currentItems = items != null ? items : new ArrayList<>();
            adapter.submitList(currentItems);
            emptyText.setVisibility(currentItems.isEmpty() ? View.VISIBLE : View.GONE);
            updateExpiryBanner();
        });

        findViewById(R.id.fabAddItem).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditPantryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Settings may have changed while the user was on the Settings screen.
        adapter.setExpirySettings(AppSettings.isExpiryAlertsOn(this), AppSettings.getExpiryDays(this));
        updateExpiryBanner();
    }

    /** Shows "2 items expire within 3 days" at the top when alerts are switched on. */
    private void updateExpiryBanner() {
        if (!AppSettings.isExpiryAlertsOn(this)) {
            expiryBanner.setVisibility(View.GONE);
            return;
        }
        int days = AppSettings.getExpiryDays(this);
        int count = 0;
        for (PantryItem item : currentItems) {
            if (item.expiryDate != null && PantryAdapter.daysUntil(item.expiryDate) <= days) {
                count++;
            }
        }
        if (count == 0) {
            expiryBanner.setVisibility(View.GONE);
        } else {
            expiryBanner.setText(getResources().getQuantityString(
                    R.plurals.expiry_banner, count, count, days));
            expiryBanner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(PantryItem item) {
        Intent intent = new Intent(this, AddEditPantryActivity.class);
        intent.putExtra(AddEditPantryActivity.EXTRA_ITEM_ID, item.id);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
        // Ask first, so one mis-tap doesn't delete an ingredient.
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, item.name))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    repository.delete(item);
                    Toast.makeText(this, getString(R.string.msg_item_deleted, item.name),
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
