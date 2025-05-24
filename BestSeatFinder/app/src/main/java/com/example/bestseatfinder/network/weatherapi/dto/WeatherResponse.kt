package com.example.bestseatfinder.network.weatherapi.dto

import com.google.gson.annotations.SerializedName

// Top-level response object
data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherDescription>,
    @SerializedName("main") val main: MainDetails,
    @SerializedName("clouds") val clouds: CloudCoverage,
    @SerializedName("name") val cityName: String? // Optional: City name from response
)

data class WeatherDescription(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String, // e.g., "Clouds", "Clear", "Rain"
    @SerializedName("description") val description: String, // e.g., "few clouds", "clear sky"
    @SerializedName("icon") val icon: String
)

data class MainDetails(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class CloudCoverage(
    @SerializedName("all") val all: Int // Cloudiness percentage
)

// Simplified class for the app's internal use
data class WeatherCondition(
    val mainCondition: String,          // e.g., "Clouds"
    val description: String,            // e.g., "scattered clouds"
    val cloudCoverPercentage: Int,      // e.g., 40
    val temperature: Double,
    val feelsLike: Double
)
