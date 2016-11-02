package ru.jooogle.sunshine.admin_pc.sunshine_reborn.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.R;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.utils.Utility;

public class WeatherRealmAdapter extends RealmRecyclerViewAdapter<Weather, WeatherRealmAdapter.WeatherRealmViewHolder> {
    private static final String TAG = "WeatherRealmAdapter";

    private final int VIEW_TYPE_COUNT = 2;
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    public WeatherRealmAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Weather> data) {
        super(context, data, true);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public WeatherRealmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;

        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else if (viewType == VIEW_TYPE_FUTURE_DAY)
            layoutId = R.layout.list_item_forecast;

        View view = inflater.inflate(layoutId, parent, false);
        view.setOnClickListener(v -> Log.d("WeatherRealmAdapter", "onClick"));

        return new WeatherRealmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeatherRealmViewHolder holder, int position) {
        Weather weather = getData().get(position);

        int weatherId = (int) weather.getWeatherId();

        int resId = -1;
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_TODAY) {
            resId = Utility.getArtResourceForWeatherCondition(weatherId);
        } else if (viewType == VIEW_TYPE_FUTURE_DAY) {
            resId = Utility.getIconResourceForWeatherCondition(weatherId);
        }
        holder.iconView.setImageResource(resId);

        long dateInMillis = weather.getDate();

        holder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        String description = weather.getDescription();

        holder.descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        double high = weather.getMaxTemp();
        holder.highTempView.setText(Utility.formatTemperature(context, high, true)); // TODO isMetric instead true

        double low = weather.getMinTemp();
        holder.lowTempView.setText(Utility.formatTemperature(context, low, true));

    }

    class WeatherRealmViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_icon)
        ImageView iconView;
        @BindView(R.id.list_item_date_textview)
        TextView dateView;
        @BindView(R.id.list_item_forecast_textview)
        TextView descriptionView;
        @BindView(R.id.list_item_high_textview)
        TextView highTempView;
        @BindView(R.id.list_item_low_textview)
        TextView lowTempView;

        WeatherRealmViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
