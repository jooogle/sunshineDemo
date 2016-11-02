package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.sync.jobscheduler;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.utils.Utility;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.SunshineActivity;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SyncService extends JobService {
	private static final String TAG = "SyncService";
	private static final int WEATHER_NOTIFICATION_ID = 3004;

	private static final long SYNC_INTERVAL = 1000 * 10 * 1;

	private Subscription mSyncSub;

	private RealmResults<Weather> mWeather;

	private static boolean mIsLollipopOrHigher;
	private static final int JOB_SERVICE_ID = 1;
	private static final int JOB_SERVICE_IMMEDIATELY_ID = 2;

	public static Intent newIntent(Context context) {
		return new Intent(context, SyncService.class);
	}

	public static void setServiceAlarm(Context context, boolean isOn) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setJobScheduler(context, isOn);
			setAlarmManager(context, isOn);
			mIsLollipopOrHigher = true;
		} else {
			setAlarmManager(context, isOn);
			mIsLollipopOrHigher = false;
		}
	}

	private static void setJobScheduler(Context context, boolean isOn) {
		Log.d(TAG, "setJobScheduler");

		JobScheduler jobScheduler = (JobScheduler)
				context.getSystemService(JOB_SCHEDULER_SERVICE);

		if (isOn) {
			Log.d(TAG, "isOn");

			JobInfo jobInfo = new JobInfo.Builder(JOB_SERVICE_ID, new ComponentName(context.getPackageName(), SyncService.class.getName()))
					.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
					.setPeriodic(SYNC_INTERVAL)
					.setPersisted(true)
					.build();

			jobScheduler.schedule(jobInfo);

		} else {
			jobScheduler.cancel(JOB_SERVICE_ID);
		}

	}

	private static void setAlarmManager(Context context, boolean isOn) {
		Intent i = newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

		AlarmManager alarmManager =
				(AlarmManager) context.getSystemService(ALARM_SERVICE);

		if (isOn) {
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), SYNC_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}

	public static boolean isServiceAlarmOn(Context context) {
		if (mIsLollipopOrHigher) {
			return isJobSchedulerOn(context);
		} else {
			return isAlarmManagerOn(context);
		}
	}

	private static boolean isJobSchedulerOn(Context context) {
		JobScheduler scheduler = (JobScheduler)
				context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		boolean hasBeenScheduled = false;
		for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
			if (jobInfo.getId() == JOB_SERVICE_ID) {
				hasBeenScheduled = true;
			}
		}

		return hasBeenScheduled;
	}

	private static boolean isAlarmManagerOn(Context context) {
		Intent i = newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);

		return pi != null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		Log.d(TAG, "onStartJob");

		if (mSyncSub != null && !mSyncSub.isUnsubscribed()) {
			mSyncSub.unsubscribe();
		}

		DataManager dm = SunshineApplication.get(this).getDataManager();
		mSyncSub = dm.syncWeather()
				.subscribeOn(Schedulers.io())
				.subscribe(new Subscriber<Weather>() {
					@Override
					public void onCompleted() {
						notifyWeather(SyncService.this);
						jobFinished(params, false);

						Log.d(TAG, "jobFinished");
					}

					@Override
					public void onError(Throwable e) {

					}

					@Override
					public void onNext(Weather weather) {

					}
				});

		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		Log.d(TAG, "Job stopped");

		if (mSyncSub != null) {
			mSyncSub.unsubscribe();
		}
		return true;
	}

	private void notifyWeather(Context context) {
		// Проверяем включены ли уведомления
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String displayNotificationKey = context.getString(R.string.pref_enable_notifications_key);
		boolean displayNotification = prefs.getBoolean(displayNotificationKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

		if (true) {

			// Проверяем последнее обновление и уведомляем, если это первое за день
			String lastNotificationKey = context.getString(R.string.pref_last_notification);
			long lastSync = prefs.getLong(lastNotificationKey, 0);


			if (true) {
				// Последняя синхронизация была более одного дня назад, отправляем уведомление о погоде.
				String locationQuery = PreferenceHelper.getStoredLocation(context);

				DataManager dataManager = SunshineApplication.get(context).getDataManager();
				Realm realm = Realm.getDefaultInstance();

				RealmResults<Weather> weathers = dataManager.getWeatherForNotify(realm);
				if (!weathers.isEmpty()) {
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mSyncSub != null) {
			mSyncSub.unsubscribe();
		}
	}
}
