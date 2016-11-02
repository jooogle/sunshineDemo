package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local;

import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import rx.Observable;

public class DatabaseHelper {
	private static final String TAG = "DatabaseHelper";

	public DatabaseHelper() {
	}

	public Observable<Weather> setWeatherForecast(final List<Weather> weatherForecasts) {
		return Observable.create(subscriber -> {
			if (subscriber.isUnsubscribed())
				return;

			Realm realm = null;
			try {
				realm = Realm.getDefaultInstance();
				realm.beginTransaction();
				for (int i = 0; i < weatherForecasts.size(); ++i) {
					weatherForecasts.get(i).setId(i);
					realm.copyToRealmOrUpdate(weatherForecasts.get(i));
				}
				realm.commitTransaction();

				subscriber.onCompleted();
			} catch (Exception e) {
				subscriber.onError(e);
			} finally {
				if (realm != null) {
					realm.close();
				}
			}
		});
	}

	/*
	 * Достаем из базы данные о местоположении и погоде на все дни
	 */
	public Observable<RealmResults<Weather>> getWeatherForecast(Realm realm) {
		realm = Realm.getDefaultInstance();

		Log.d(TAG, "getWeatherForecast");

		return realm.where(Weather.class).findAllAsync().asObservable()
				.filter(RealmResults::isLoaded)
				.first();
	}

	public RealmResults<Weather> getSyncWeatherForecast(Realm realm) {
		realm = Realm.getDefaultInstance();

		return realm.where(Weather.class).findAll();
	}
}
