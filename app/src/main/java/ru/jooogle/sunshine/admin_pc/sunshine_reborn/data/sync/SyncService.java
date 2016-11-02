package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.sync;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.R;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.SunshineApplication;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.DataManager;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.PreferenceHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.utils.NetworkUtils;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.utils.Utility;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.SunshineActivity;
import rx.Subscriber;
import rx.Subscription;

public class SyncService extends IntentService {
	private static final String TAG = "PollService";

	private static final long POLL_INTERVAL = 1000 * 10;
	private static final int WEATHER_NOTIFICATION_ID = 3004;

	private Subscription mSubscription;
	private RealmResults<Weather> mWeather;

	public static Intent newIntent(Context context) {
		return new Intent(context, SyncService.class);
	}

	public static void syncImmediately(Context context) {
		Intent intent = newIntent(context);
		context.startService(intent);

		Log.d(TAG, "SyncImmediately");
	}

	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent i = newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

		AlarmManager alarmManager =
				(AlarmManager) context.getSystemService(ALARM_SERVICE);

		if (isOn) {
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}

	public static boolean isServiceAlarmOn(Context context) {
		Intent i = newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

		return pi != null;
	}

	private void notifyWeather(Context context) {
		// Проверяем включены ли уведомления
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String displayNotificationKey = context.getString(R.string.pref_enable_notifications_key);
		boolean displayNotification = prefs.getBoolean(displayNotificationKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

		// TODO Пока не проверяется.
		if (true) {

			// Проверяем последнее обновление и уведомляем, если это первое за день
			String lastNotificationKey = context.getString(R.string.pref_last_notification);
			long lastSync = prefs.getLong(lastNotificationKey, 0);

			// TODO пока не проверяется
			if (true) {
				// Последняя синхронизация была более одного дня назад, отправляем уведомление о погоде.
				String locationQuery = PreferenceHelper.getStoredLocation(context);

				DataManager dataManager = SunshineApplication.get(context).getDataManager();
				Realm realm = Realm.getDefaultInstance();

				RealmResults<Weather> weathers = dataManager.getWeatherForNotify(realm);
				if (!weathers.isEmpty()) {
					Log.d(TAG, "onComplete");

					Weather weather = weathers.first();

					long weatherId = weather.getWeatherId();
					double high = weather.getMaxTemp();
					double low = weather.getMinTemp();
					String desc = weather.getDescription();

					int iconId = Utility.getIconResourceForWeatherCondition((int) weatherId);
					String title = context.getString(R.string.app_name);

					boolean isMetric = Utility.isMetric(context);
					// Объявляем тектс данных.
					String contentText = String.format(context.getString(R.string.format_notification),
							desc,
							Utility.formatTemperature(context, high, isMetric),
							Utility.formatTemperature(context, low, isMetric));

					// Сделать события нажатия пользователем на уведомнение
					// В этом случае открываем приложение
					Intent intent = SunshineActivity.newIntent(context);
					PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

					Notification notification = new NotificationCompat.Builder(this)
							.setSmallIcon(iconId)
							.setContentTitle(title)
							.setContentText(contentText)
							.setContentIntent(pendingIntent)
							.setAutoCancel(true)
							.build();

					NotificationManagerCompat notificationManager =
							NotificationManagerCompat.from(this);

					notificationManager.notify(WEATHER_NOTIFICATION_ID, notification);

					// Обновляем данные о последней синхронизации
					SharedPreferences.Editor editor = prefs.edit();
					editor.putLong(lastNotificationKey, System.currentTimeMillis());
					editor.apply();
				}

				realm.close();
			}
		}
	}

	public SyncService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!NetworkUtils.isNetworkAvailableAndConnected(this))
			return;

		DataManager dataManager = SunshineApplication.get(this).getDataManager();

		if (mSubscription != null && !mSubscription.isUnsubscribed())
			mSubscription.unsubscribe();

		mSubscription = dataManager.syncWeather()
				.subscribe(new Subscriber<Weather>() {
					@Override
					public void onCompleted() {
						Log.d(TAG, "syncCompleted");
						notifyWeather(SyncService.this);
					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onNext(Weather weather) {

					}
				});

		Log.i(TAG, "Received an intent: " + intent);
	}

	@Override
	public void onDestroy() {
		if (mSubscription != null) mSubscription.unsubscribe();
		super.onDestroy();
	}
}
