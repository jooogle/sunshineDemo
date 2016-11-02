package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.local.DatabaseHelper;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Location;
import ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model.Weather;

public class WeatherJsonConverter {
    private static final String LOG_TAG = WeatherJsonConverter.class.getSimpleName();
    private DatabaseHelper mDatabaseHelper;

    public WeatherJsonConverter(DatabaseHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }

    /*
            Извлекаем необходимые данные из результатов запроса к
            сервису openWeatherMap.
         */
    public List<Weather> getDailyWeatherFromJson(ResponseBody responseBody) {
        try {
            return getDailyWeatherFromJson(responseBody.string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Weather> getDailyWeatherFromJson(String forecastJsonStr) {
        Log.d(LOG_TAG, "Forecast string: " + forecastJsonStr);

        // Имена объектов JSON, которые должны быть извлечены

        // Информация о расположении
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Координаты расположения
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Информация о погоде. Информация о каждом дне представленна в массиве "list"
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // Все температуры дети объекта "temp"
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            // Извлекаем данные о местоположении
            Location location = new Location();
            location.setLongitude(cityLongitude);
            location.setLatitude(cityLatitude);
            location.setLocationSettings(cityName);
            location.setCity(cityName);

            // Извлекаем данные о погоде
            ArrayList<Weather> weatherVector = new ArrayList<>(weatherArray.length());

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            Log.d(LOG_TAG, "Weather array length: " + weatherArray.length());

            for (int i = 0; i < weatherArray.length(); i++) {
                // Данные для сохранения
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Получаем объект JSON, представляющий день
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // День недели
                dateTime = dayTime.setJulianDay(julianStartDay + i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // Описание в дочернем массиве "weather", из одного элемента long
                // Этот элемент также хранит weather код
                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Температуры это дети элемента "temp"
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                // Создаем объект погоды, добавляем в вектор, потом вставляем в базу
                Weather weather = new Weather();

                weather.setLocation(location);
                weather.setDate(dateTime);
                weather.setHumidity(humidity);
                weather.setPressure(pressure);
                weather.setWindSpeed(windSpeed);
                weather.setDegrees(windDirection);
                weather.setMaxTemp(high);
                weather.setMinTemp(low);
                weather.setDescription(description);
                weather.setWeatherId(weatherId);

                weatherVector.add(weather);
            }

            Log.d(LOG_TAG, "Weather vector size: " + weatherVector.size());
            return weatherVector;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }
}
