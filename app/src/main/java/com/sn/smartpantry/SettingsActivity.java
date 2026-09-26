package com.sn.smartpantry;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings screen. Changes are saved as soon as the user makes them,
 * so there is no Save button.
 */
public class SettingsActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup daysGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialSwitch expirySwitch = findViewById(R.id.expirySwitch);
        daysGroup = findViewById(R.id.daysGroup);

        // Show the saved values.
        boolean alertsOn = AppSettings.isExpiryAlertsOn(this);
        expirySwitch.setChecked(alertsOn);
        daysGroup.check(buttonIdForDays(AppSettings.getExpiryDays(this)));
        setDaysEnabled(alertsOn);

        // Save each change straight away.
        expirySwitch.setOnCheckedChangeListener((button, isChecked) -> {
            AppSettings.setExpiryAlertsOn(this, isChecked);
            setDaysEnabled(isChecked);
        });

        daysGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                AppSettings.setExpiryDays(this, daysForButtonId(checkedId));
            }
        });
    }

    /** The day choice only matters when alerts are on, so grey it out otherwise. */
    private void setDaysEnabled(boolean enabled) {
        for (int i = 0; i < daysGroup.getChildCount(); i++) {
            daysGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    private static int buttonIdForDays(int days) {
        if (days == 1) {
            return R.id.days1Button;
        } else if (days == 7) {
            return R.id.days7Button;
        }
        return R.id.days3Button;
    }

    private static int daysForButtonId(int buttonId) {
        if (buttonId == R.id.days1Button) {
            return 1;
        } else if (buttonId == R.id.days7Button) {
            return 7;
        }
        return 3;
    }
}
