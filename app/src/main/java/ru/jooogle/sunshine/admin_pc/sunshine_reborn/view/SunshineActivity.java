package ru.jooogle.sunshine.admin_pc.sunshine_reborn.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import butterknife.ButterKnife;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.R;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.view.Fragments.ForecastFragment;

public class SunshineActivity extends AppCompatActivity {
    private static final String TAG = "SunshineActivity";

    private boolean mTwoPane;

    public static Intent newIntent(Context context) {
        return new Intent(context, SunshineActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setLogo(R.drawable.ic_logo);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {

            }

        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);
    }
}
