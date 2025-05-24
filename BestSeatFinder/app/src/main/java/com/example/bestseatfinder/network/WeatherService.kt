package com.example.bestseatfinder.network

import com.example.bestseatfinder.network.weatherapi.OpenWeatherMapApiService
import com.example.bestseatfinder.network.weatherapi.dto.WeatherCondition
import com.example.bestseatfinder.network.weatherapi.dto.WeatherResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WeatherService {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

import com.example.bestseatfinder.BuildConfig // Import BuildConfig

object WeatherService {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    // API Key is now accessed via BuildConfig
    private val API_KEY = BuildConfig.OPENWEATHERMAP_API_KEY

    private val retrofit: Retrofit

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val weatherApiService: OpenWeatherMapApiService by lazy {
        retrofit.create(OpenWeatherMapApiService::class.java)
    }

    /**
     * Fetches the current weather for a given latitude and longitude.
     *
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return A simplified [WeatherCondition] object if successful, null otherwise.
     *         In case of an error, a message will be printed to the console.
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherCondition? {
        if (API_KEY.isBlank() || API_KEY == "YOUR_DEFAULT_API_KEY_IF_NOT_FOUND") {
            println("ERROR: API Key for OpenWeatherMap is missing or is the default placeholder. Please ensure it's set correctly in local.properties and Gradle is synced.")
            return null
        }

        return try {
            val response: retrofit2.Response<WeatherResponse> = weatherApiService.getCurrentWeather(
                lat = latitude,
                lon = longitude,
                apiKey = API_KEY
            )

            if (response.isSuccessful) {
                val weatherResponse = response.body()
                if (weatherResponse != null) {
                    val mainDesc = weatherResponse.weather.firstOrNull()?.main ?: "Unknown"
                    val detailedDesc = weatherResponse.weather.firstOrNull()?.description ?: "No description"
                    val cloudCover = weatherResponse.clouds.all
                    val temp = weatherResponse.main.temp
                    val feelsLike = weatherResponse.main.feelsLike

                    WeatherCondition(
                        mainCondition = mainDesc,
                        description = detailedDesc,
                        cloudCoverPercentage = cloudCover,
                        temperature = temp,
                        feelsLike = feelsLike
                    )
                } else {
                    println("Error: Empty response body from OpenWeatherMap API.")
                    null
                }
            } else {
                println("Error: API call not successful. Code: ${response.code()}, Message: ${response.message()}")
                println("Response body: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("Exception during weather API call: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
