package com.android.weather.apis

import com.android.weather.BuildConfig
import com.android.weather.models.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Weather APIs : BASE URL : https://api.openweathermap.org/data/2.5/
 */
interface WeatherApi {
    @GET("weather")
    suspend fun getSearch(
        @Query("apikey") apiKey: String = BuildConfig.API_KEY,
        @Query("q") search: String? = null
    ): SearchResponse
}