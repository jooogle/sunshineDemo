package ru.jooogle.sunshine.admin_pc.sunshine_reborn.presenter;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.SunshineApplication;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.DataManager;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.SunshineMvpView;
import rx.Subscription;

public class SunshinePresenter implements Presenter<SunshineMvpView> {
	public static String TAG = "SunshinePresenter";

	private SunshineMvpView mSunshineMvpView;
	private Subscription mSubscription;
	private RealmResults<Weather> mWeatherForecast;

	private Realm mRealm;

	@Override
	public void attachView(SunshineMvpView view) {
		mSunshineMvpView = view;
	}

	@Override
	public void detachView() {
		mSunshineMvpView = null;

		if (mSubscription != null && !mSubscription.isUnsubscribed()) {
			mSubscription.unsubscribe();
		}
	}

	public void loadWeather() {
		Log.d(TAG, "loadWeather");

		mSunshineMvpView.showProgressIndicator();
		if (mSubscription != null)
			mSubscription.unsubscribe();

		// TODO may be get DataManager from Application instance?
		DataManager manager = SunshineApplication.get(mSunshineMvpView.getContext()).getDataManager();
		mSubscription = manager.getWeather(mRealm)
				.subscribe(
						weathers -> {
							Log.d(TAG, "getWeather");
							mWeatherForecast = weathers;
						},
						error -> {
							// TODO detect error type and show right message
						},
						() -> {
							Log.d(TAG, "getWeatherCompleted");
							mSunshineMvpView.showWeather(mWeatherForecast);
						});
	}

	// Only test. To be deleted
	public void syncWeather() {
		DataManager manager = SunshineApplication.get(mSunshineMvpView.getContext()).getDataManager();
		manager.syncWeather()
				.subscribe();
	}

}
