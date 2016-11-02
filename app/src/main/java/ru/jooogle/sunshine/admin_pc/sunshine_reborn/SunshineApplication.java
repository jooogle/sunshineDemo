package ru.jooogle.sunshine.admin_pc.sunshine_reborn;

import android.app.Application;
import android.content.Context;
import android.widget.Button;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.DataManager;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.DatabaseHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.PreferenceHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote.NetworkService;

public class SunshineApplication extends Application {
    private NetworkService networkService;
    private DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // Set location. To be deleted
        PreferenceHelper.setStoredLocation(this, "Rostov-on-Don");
    }

    public static SunshineApplication get(Context context) {
        return (SunshineApplication) context.getApplicationContext();
    }

    public NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = new NetworkService();
        }

        return networkService;
    }

    public DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager(getNetworkService(), new DatabaseHelper(), this);
        }

        return dataManager;
    }
}
