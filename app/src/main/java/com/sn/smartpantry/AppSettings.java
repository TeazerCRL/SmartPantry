package com.sn.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Reads and writes the user's settings. Settings are small key-value pairs,
 * so they live in SharedPreferences rather than the Room database.
 */
public final class AppSettings {

    public static final int DEFAULT_EXPIRY_DAYS = 3;

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_EXPIRY_DAYS = "expiry_days";

    private AppSettings() {
        // Utility class: only static methods.
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Whether items close to their expiry date should be highlighted. On by default. */
    public static boolean isExpiryAlertsOn(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsOn(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, on).apply();
    }

    /** How many days ahead counts as "expiring soon". */
    public static int getExpiryDays(Context context) {
        return prefs(context).getInt(KEY_EXPIRY_DAYS, DEFAULT_EXPIRY_DAYS);
    }

    public static void setExpiryDays(Context context, int days) {
        prefs(context).edit().putInt(KEY_EXPIRY_DAYS, days).apply();
    }
}
