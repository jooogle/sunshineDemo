package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote;

import android.util.LruCache;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class NetworkService {
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/";
    private static final int LRU_CASH_SIZE = 10;

    private OpenWeatherMapService openWeatherMapService;
    private OkHttpClient okHttpClient;
    private LruCache<Class<?>, Observable<?>> cachedObservables;

    public NetworkService() {
        this(BASE_URL);
    }

    public NetworkService(String baseUrl) {
        okHttpClient = buildClient();
        cachedObservables = new LruCache<>(LRU_CASH_SIZE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        openWeatherMapService = retrofit.create(OpenWeatherMapService.class);
    }

    public OpenWeatherMapService openWeatherMapService() {
        return openWeatherMapService;
    }

    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(chain -> {
            Response response = chain.proceed(chain.request());

            return response;
        });

        builder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Accept", "application/json")
                    .build();

            return chain.proceed(request);
        });

        return builder.build();
    }

    public void clearCache() {
        cachedObservables.evictAll();
    }

    public Observable<?> getPreparedObservable(Observable<?> unPreparedObservable, Class<?> clazz) {
        return getPreparedObservable(unPreparedObservable, clazz, true, true);
    }

    public Observable<?> getPreparedObservable(Observable<?> unPreparedObservable, Class<?> clazz,
                                               boolean isCacheObservable, boolean isUseCache) {
        Observable<?> preparedObservable = null;

        if (isUseCache) {
            preparedObservable = cachedObservables.get(clazz);
        }

        if (preparedObservable != null) {
            return preparedObservable;
        }

        preparedObservable = unPreparedObservable;

        if (isCacheObservable) {
            preparedObservable = preparedObservable.cache();
            cachedObservables.put(clazz, preparedObservable);
        }

        return preparedObservable;
    }
}
