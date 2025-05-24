package com.example.bestseatfinder.logic

import com.example.bestseatfinder.network.weatherapi.dto.WeatherCondition
import org.junit.Assert.assertTrue
import org.junit.Test

class SeatLogicTest {

    @Test
    fun getFinalRecommendation_clearWeather_sunOnRight_train() {
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 90.0, // Sun East
            travelBearing = 0.0, // Traveling North
            weather = null, // Clear
            transportMode = "Train"
        )
        println("SeatLogicTest - Clear, Sun Right, Train: $recommendation")
        assertTrue("Should recommend Sit Left", recommendation.contains("Sit Left (Sun on Right)"))
        assertTrue("Should mention clear skies or similar", recommendation.contains("(Clear Skies)"))
    }

    @Test
    fun getFinalRecommendation_clearWeather_sunOnLeft_bus() {
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 270.0, // Sun West
            travelBearing = 0.0, // Traveling North
            weather = null,
            transportMode = "Bus"
        )
        println("SeatLogicTest - Clear, Sun Left, Bus: $recommendation")
        assertTrue("Should recommend Sit Right", recommendation.contains("Sit Right (Sun on Left)"))
        assertTrue("Should mention clear skies or similar", recommendation.contains("(Clear Skies)"))
    }

    @Test
    fun getFinalRecommendation_overcastWeather_flight() {
        val weather = WeatherCondition("Clouds", "overcast clouds", 80, 15.0, 14.0)
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 90.0,
            travelBearing = 0.0,
            weather = weather,
            transportMode = "Flight"
        )
        println("SeatLogicTest - Overcast, Flight: $recommendation")
        assertTrue("Should be Neutral due to overcast", recommendation.contains("Neutral / Either Side (Overcast: 80% clouds)"))
        assertTrue("Should contain flight view advice", recommendation.contains("Views on flights are variable"))
    }

    @Test
    fun getFinalRecommendation_rainyWeather_bus() {
        val weather = WeatherCondition("Rain", "light rain", 50, 10.0, 9.0)
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 90.0,
            travelBearing = 0.0,
            weather = weather,
            transportMode = "Bus"
        )
        println("SeatLogicTest - Rainy, Bus: $recommendation")
        assertTrue("Should be Neutral due to rain", recommendation.contains("Neutral / Either Side (Weather: Rain)"))
        assertTrue("Should contain bus view advice", recommendation.contains("prefer the right side"))
    }

    @Test
    fun getFinalRecommendation_sunBehind_train_clearWeather() {
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 180.0, // Sun South
            travelBearing = 0.0,  // Traveling North (Sun is directly behind)
            weather = null,
            transportMode = "Train"
        )
        println("SeatLogicTest - Sun Behind, Train, Clear: $recommendation")
        assertTrue("Should be Neutral due to sun angle", recommendation.contains("Neutral / Either Side (Sun Ahead/Behind)"))
        assertTrue("Should mention clear skies", recommendation.contains("(Clear Skies)"))
        assertTrue("Should contain train view advice", recommendation.contains("prefer the right side"))
    }

    @Test
    fun getFinalRecommendation_sunAhead_flight_cloudyNotObscuring() {
         val weather = WeatherCondition("Clouds", "few clouds", 30, 20.0, 19.0)
        val recommendation = SeatLogic.getFinalRecommendation(
            sunAzimuth = 0.0,   // Sun North
            travelBearing = 0.0,  // Traveling North (Sun is directly ahead)
            weather = weather,
            transportMode = "Flight"
        )
        println("SeatLogicTest - Sun Ahead, Flight, Cloudy: $recommendation")
        assertTrue("Should be Neutral due to sun angle", recommendation.contains("Neutral / Either Side (Sun Ahead/Behind)"))
        assertTrue("Should mention weather", recommendation.contains("(Weather: few clouds, 30% clouds)"))
        assertTrue("Should contain flight view advice", recommendation.contains("Views on flights are variable"))
    }
}
