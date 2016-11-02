package ru.jooogle.sunshine.admin_pc.sunshine_reborn.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class NetworkUtils {
    public static boolean isNetworkAvailableAndConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
