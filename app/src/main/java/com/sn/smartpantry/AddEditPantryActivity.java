package com.sn.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sn.smartpantry.data.PantryRepository;
import com.sn.smartpantry.data.entity.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * One screen used for both adding a new pantry item and editing an existing one.
 * If the launching Intent carries EXTRA_ITEM_ID, the screen is in edit mode.
 */
public class AddEditPantryActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private static final int NO_ID = -1;
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100000;
    private static final String STATE_EXPIRY = "state_expiry";

    private PantryRepository repository;

    private TextInputLayout nameLayout, quantityLayout, unitLayout;
    private TextInputEditText nameInput, quantityInput, expiryInput;
    private MaterialAutoCompleteTextView unitDropdown;

    private List<String> validUnits;
    private int itemId = NO_ID;
    private Long selectedExpiry = null; // epoch millis, null = no expiry date

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_pantry);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addEditRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        repository = new PantryRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        unitLayout = findViewById(R.id.unitLayout);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        expiryInput = findViewById(R.id.expiryInput);
        unitDropdown = findViewById(R.id.unitDropdown);

        // Units come from a fixed list so pantry units always match the units
        // used in the seeded recipes (important for the matching logic).
        String[] units = getResources().getStringArray(R.array.pantry_units);
        validUnits = Arrays.asList(units);
        unitDropdown.setSimpleItems(units);

        expiryInput.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.clearExpiryButton).setOnClickListener(v -> {
            selectedExpiry = null;
            expiryInput.setText("");
        });
        findViewById(R.id.saveButton).setOnClickListener(v -> saveItem());

        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, NO_ID);

        if (savedInstanceState != null) {
            // Screen was recreated (e.g. rotation). The text fields restore
            // themselves, but the expiry date lives in a field, so restore it here.
            if (savedInstanceState.containsKey(STATE_EXPIRY)) {
                selectedExpiry = savedInstanceState.getLong(STATE_EXPIRY);
            }
        }

        if (itemId == NO_ID) {
            toolbar.setTitle(R.string.title_add_ingredient);
            if (savedInstanceState == null) {
                unitDropdown.setText(units[0], false);
            }
        } else {
            toolbar.setTitle(R.string.title_edit_ingredient);
            // Only load from the database on first creation, otherwise we
            // would overwrite anything the user typed before rotating.
            if (savedInstanceState == null) {
                loadExistingItem();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedExpiry != null) {
            outState.putLong(STATE_EXPIRY, selectedExpiry);
        }
    }

    /** Reads the item once from Room and fills the form with its values. */
    private void loadExistingItem() {
        LiveData<PantryItem> liveItem = repository.getItemById(itemId);
        liveItem.observe(this, new Observer<PantryItem>() {
            @Override
            public void onChanged(PantryItem item) {
                liveItem.removeObserver(this); // we only need the first value
                if (item == null) {
                    Toast.makeText(AddEditPantryActivity.this,
                            R.string.msg_item_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                nameInput.setText(item.name);
                quantityInput.setText(formatQuantity(item.quantity));
                unitDropdown.setText(item.unit, false);
                selectedExpiry = item.expiryDate;
                if (selectedExpiry != null) {
                    expiryInput.setText(dateFormat.format(new Date(selectedExpiry)));
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiry != null) {
            calendar.setTimeInMillis(selectedExpiry);
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day, 23, 59, 59);
            chosen.set(Calendar.MILLISECOND, 0);
            selectedExpiry = chosen.getTimeInMillis();
            expiryInput.setText(dateFormat.format(new Date(selectedExpiry)));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // An expiry date in the past makes no sense for a new entry.
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    /** Validates every field, showing an error on each invalid one, then saves. */
    private void saveItem() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        unitLayout.setError(null);

        // Collapse repeated spaces so " red   onion " is stored as "red onion".
        String name = textOf(nameInput).trim().replaceAll("\\s+", " ");
        // Accept "0,5" as well as "0.5" (common on South African keyboards).
        String quantityText = textOf(quantityInput).trim().replace(',', '.');
        String unit = unitDropdown.getText().toString().trim();

        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            nameLayout.setError(getString(R.string.error_name_too_long, MAX_NAME_LENGTH));
            valid = false;
        } else if (!name.matches("[\\p{L} '\\-]+")) {
            nameLayout.setError(getString(R.string.error_name_letters));
            valid = false;
        }

        double quantity = 0;
        if (quantityText.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    quantityLayout.setError(getString(R.string.error_quantity_positive));
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    quantityLayout.setError(getString(R.string.error_quantity_too_large));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityLayout.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            }
        }

        if (!validUnits.contains(unit)) {
            unitLayout.setError(getString(R.string.error_unit_required));
            valid = false;
        }

        if (!valid) {
            return;
        }

        PantryItem item = new PantryItem(name, quantity, unit, selectedExpiry);
        if (itemId == NO_ID) {
            repository.insert(item);
            Toast.makeText(this, getString(R.string.msg_item_added, name), Toast.LENGTH_SHORT).show();
        } else {
            item.id = itemId; // keeps the same row so Room updates instead of inserting
            repository.update(item);
            Toast.makeText(this, getString(R.string.msg_item_updated, name), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    /** Shows 2.0 as "2" but keeps 0.5 as "0.5". */
    private static String formatQuantity(double quantity) {
        return quantity == Math.floor(quantity)
                ? String.valueOf((int) quantity)
                : String.valueOf(quantity);
    }
}
