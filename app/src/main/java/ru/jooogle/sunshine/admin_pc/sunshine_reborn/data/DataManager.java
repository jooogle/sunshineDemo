package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data;

import android.content.Context;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.DatabaseHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.PreferenceHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote.NetworkService;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote.WeatherJsonConverter;
import rx.Observable;

public class DataManager {
	private static final String TAG = "DataManager";

	private final NetworkService mNetworkService;
	private final DatabaseHelper mDatabaseHelper;

	private final Context mContext;

	public DataManager(NetworkService mNetworkService, DatabaseHelper mDatabaseHelper, Context context) {
		this.mNetworkService = mNetworkService;
		this.mDatabaseHelper = mDatabaseHelper;
		this.mContext = context;
	}

	public Observable<Weather> syncWeather() {
		Log.d(TAG, "syncWeahter");

		// TODO extract this values from SharedPreferences
		String location = PreferenceHelper.getStoredLocation(mContext);
		String units = "metric";
		String mode = "json";
		String cnt = "14";
		String appId = "0825d6fb5a6b8c2723593339f364d165";

		Observable<ResponseBody> weatherJsonBodyObservable = mNetworkService.openWeatherMapService()
				.getWeatherString(location, mode, units, cnt, appId);

		WeatherJsonConverter weatherJsonConverter = new WeatherJsonConverter(mDatabaseHelper);

		return mNetworkService.getPreparedObservable(weatherJsonBodyObservable, ResponseBody.class)
				.map(forecast -> weatherJsonConverter.getDailyWeatherFromJson((ResponseBody) forecast))
				.concatMap(mDatabaseHelper::setWeatherForecast);
	}

	public Observable<RealmResults<Weather>> getWeather(Realm realm) {
		return mDatabaseHelper.getWeatherForecast(realm);
	}

	public RealmResults<Weather> getWeatherForNotify(Realm realm) {
		return mDatabaseHelper.getSyncWeatherForecast(realm);
	}
}
