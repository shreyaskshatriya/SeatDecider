package com.example.bestseatfinder.logic

import com.example.bestseatfinder.network.weatherapi.dto.WeatherCondition

object SeatLogic {

    private const val CLOUD_COVER_THRESHOLD = 75 // Percentage
    private val SUN_OBSCURING_WEATHER_CONDITIONS = listOf(
        "RAIN", "SNOW", "DRIZZLE", "THUNDERSTORM", "FOG", "MIST", "HAZE", "SQUALL"
    )

    /**
     * Provides a final seat recommendation considering sun, weather, and view preferences.
     *
     * @param sunAzimuth The sun's azimuth in degrees.
     * @param travelBearing The direction of travel (bearing) in degrees.
     * @param weather Optional weather condition data.
     * @param transportMode The mode of transport (e.g., "Bus", "Train", "Flight").
     * @return A comprehensive string recommendation.
     */
    fun getFinalRecommendation(
        sunAzimuth: Double,
        travelBearing: Double,
        weather: WeatherCondition?,
        transportMode: String
    ): String {
        var sunWeatherRecommendation = getSunWeatherRecommendation(sunAzimuth, travelBearing, weather)

        // Apply view preference logic if sun/weather recommendation is neutral
        if (sunWeatherRecommendation.startsWith("Neutral / Either Side")) {
            sunWeatherRecommendation = when {
                transportMode.equals("Train", ignoreCase = true) ||
                transportMode.equals("Bus", ignoreCase = true) -> {
                    "$sunWeatherRecommendation. For potentially better views on a ${transportMode.lowercase()}, some prefer the right side."
                }
                transportMode.equals("Flight", ignoreCase = true) -> {
                    "$sunWeatherRecommendation. Views on flights are variable; check seat maps and flight path."
                }
                else -> sunWeatherRecommendation // No specific view preference for other modes
            }
        }
        return sunWeatherRecommendation
    }

    /**
     * Determines seat recommendation based purely on sun position and current weather.
     */
    private fun getSunWeatherRecommendation(
        sunAzimuth: Double,
        travelBearing: Double,
        weather: WeatherCondition?
    ): String {
        weather?.let {
            if (it.cloudCoverPercentage > CLOUD_COVER_THRESHOLD) {
                return "Neutral / Either Side (Overcast: ${it.cloudCoverPercentage}% clouds)"
            }
            if (SUN_OBSCURING_WEATHER_CONDITIONS.any { cond -> it.mainCondition.equals(cond, ignoreCase = true) }) {
                return "Neutral / Either Side (Weather: ${it.mainCondition})"
            }
        }

        // Normalize bearings to be 0-360
        val normalizedSunAzimuth = (sunAzimuth % 360 + 360) % 360
        val normalizedTravelBearing = (travelBearing % 360 + 360) % 360

        // Calculate the relative angle of the sun with respect to the direction of travel.
        val relativeSunAngle = (normalizedSunAzimuth - normalizedTravelBearing + 360) % 360

        // Define angle ranges for left, right, ahead, behind.
        val aheadBuffer = 15.0 // degrees
        val behindBuffer = 15.0 // degrees

        val weatherReason = if (weather != null) "(Weather: ${weather.description}, ${weather.cloudCoverPercentage}% clouds)" else "(Clear Skies)"

        return when {
            relativeSunAngle > aheadBuffer && relativeSunAngle < (180 - behindBuffer) ->
                "Sit Left (Sun on Right) $weatherReason"
            relativeSunAngle > (180 + behindBuffer) && relativeSunAngle < (360 - aheadBuffer) ->
                "Sit Right (Sun on Left) $weatherReason"
            else ->
                "Neutral / Either Side (Sun Ahead/Behind) $weatherReason"
        }
    }
}
