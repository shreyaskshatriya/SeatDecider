package com.example.bestseatfinder.network.weatherapi

import com.example.bestseatfinder.network.weatherapi.dto.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // or "imperial"
    ): Response<WeatherResponse>
}
