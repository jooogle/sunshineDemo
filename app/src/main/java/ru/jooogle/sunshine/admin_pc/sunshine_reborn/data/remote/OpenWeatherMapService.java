package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.remote;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface OpenWeatherMapService {
    @GET("daily?")
    Observable<ResponseBody> getWeatherString(@Query("q") String location,
                                              @Query("mode") String mode,
                                              @Query("units") String units,
                                              @Query("cnt") String count,
                                              @Query("APPID") String appId
    );
}
