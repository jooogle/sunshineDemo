package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local;

import android.content.Context;
import android.preference.PreferenceManager;

public class PreferenceHelper {
    private static final String PREF_LOCATION = "location";

    public static String getStoredLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LOCATION, null);
    }

    public static void setStoredLocation(Context context, String location) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LOCATION, location)
                .apply();
    }
}
