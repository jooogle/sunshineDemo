package ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.realm.RealmResults;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.R;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.adapters.WeatherRealmAdapter;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.sync.SyncService;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.presenter.SunshinePresenter;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.SunshineMvpView;

public class ForecastFragment extends Fragment implements SunshineMvpView {
	private static final String TAG = "ForecastFragment";

	private SunshinePresenter mPresenter;

	private RecyclerView mWeatherRecyclerView;
	private WeatherRealmAdapter mWeatherAdapter;

	private boolean mUseTodayLayout;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mPresenter = new SunshinePresenter();
		mPresenter.attachView(this);
		mPresenter.loadWeather();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d("onCreateView", "onCreateView");
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		mWeatherRecyclerView = ButterKnife.findById(view, R.id.recyclerview_forecast);
		mWeatherRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		return view;
	}

	public void setUseTodayLayout(boolean useTodayLayout) {
		mUseTodayLayout = useTodayLayout;

		if (mWeatherAdapter != null) {
			mWeatherAdapter.setUseTodayLayout(mUseTodayLayout);
		}
	}

	@Override
	public void onDestroy() {
		mPresenter.detachView();
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_sunshine, menu);

		MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
		if (SyncService.isServiceAlarmOn(getActivity())) {
			toggleItem.setTitle(R.string.stop_polling);
		} else {
			toggleItem.setTitle(R.string.start_polling);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.menu_item_toggle_polling:
				boolean shouldStartAlarm =
						!SyncService.isServiceAlarmOn(getActivity());
				SyncService.setServiceAlarm(getActivity(), shouldStartAlarm);
				getActivity().invalidateOptionsMenu();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void showWeather(RealmResults<Weather> weathers) {
		Log.d("showWeather", "showWeather");
		// Т.к. я использую для отображения погоды RealmAdapter с RealmResults
		// то у меня нет необходимости обновлять данные в ручную
		// когда данные меняются в базе, они автоматически изменятся
		// в RealmResults и в адаптере
		if (mWeatherAdapter == null) {
			// TODO сделать синхронизацию через ресивер
			if (weathers.isEmpty()) {
				SyncService.syncImmediately(getActivity());
			}
			mWeatherAdapter = new WeatherRealmAdapter(getActivity(), weathers);
			mWeatherAdapter.setUseTodayLayout(mUseTodayLayout);
			mWeatherRecyclerView.setAdapter(mWeatherAdapter);
		}
	}

	@Override
	public void showMessage(int stringId) {
		// TODO
	}

	@Override
	public void showProgressIndicator() {
		// TODO
	}
}
