package ru.jooogle.sunshine.admin_pc.sunshine_reborn.view;

import java.util.List;

import io.realm.RealmResults;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;

public interface SunshineMvpView extends MvpView {
    void showWeather(RealmResults<Weather> weathers);

    void showMessage(int stringId);

    void showProgressIndicator();
}
