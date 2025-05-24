package com.example.bestseatfinder.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SunCalcTest {

    @Test
    fun calculateSunPosition_validInputs_returnsReasonableValues() {
        // Test case: London, UK, approx. noon on Summer Solstice
        val latitude = 51.5074 // London
        val longitude = -0.1278 // London
        val year = 2023
        val month = 6 // June
        val day = 21 // Summer Solstice
        val hour = 12 // Noon
        val minute = 0
        val timezoneOffsetHours = 1 // BST for London in June

        val (azimuth, altitude) = SunCalc.calculateSunPosition(
            latitude, longitude, year, month, day, hour, minute, timezoneOffsetHours
        )

        println("SunCalcTest - Summer Solstice London Noon: Azimuth: $azimuth, Altitude: $altitude")

        assertTrue("Azimuth should be between 0 and 360", azimuth >= 0.0 && azimuth <= 360.0)
        assertTrue("Altitude should be between -90 and 90", altitude >= -90.0 && altitude <= 90.0)
        // For London noon on summer solstice, sun should be roughly South (around 180 deg) and high.
        assertTrue("Altitude should be positive for noon in summer", altitude > 0)
    }

    @Test
    fun calculateSunPosition_midnight_returnsNegativeAltitude() {
        // Test case: London, UK, approx. midnight
        val latitude = 51.5074 // London
        val longitude = -0.1278 // London
        val year = 2023
        val month = 1 // January
        val day = 1
        val hour = 0 // Midnight
        val minute = 0
        val timezoneOffsetHours = 0 // GMT for London in January

        val (azimuth, altitude) = SunCalc.calculateSunPosition(
            latitude, longitude, year, month, day, hour, minute, timezoneOffsetHours
        )
        println("SunCalcTest - Midnight London: Azimuth: $azimuth, Altitude: $altitude")

        assertTrue("Azimuth should be between 0 and 360", azimuth >= 0.0 && azimuth <= 360.0)
        assertTrue("Altitude should be between -90 and 90", altitude >= -90.0 && altitude <= 90.0)
        assertTrue("Altitude should be negative for midnight", altitude < 0)
    }

    @Test
    fun calculateSunPosition_equatorEquinoxNoon_sunIsHigh() {
        // Test case: Equator, approx. noon on Equinox
        val latitude = 0.0 // Equator
        val longitude = 0.0
        val year = 2023
        val month = 3 // March
        val day = 20 // Approx Equinox
        val hour = 12 // Noon
        val minute = 0
        val timezoneOffsetHours = 0 // UTC

        val (azimuth, altitude) = SunCalc.calculateSunPosition(
            latitude, longitude, year, month, day, hour, minute, timezoneOffsetHours
        )
        println("SunCalcTest - Equator Equinox Noon: Azimuth: $azimuth, Altitude: $altitude")


        assertTrue("Azimuth should be between 0 and 360", azimuth >= 0.0 && azimuth <= 360.0)
        assertTrue("Altitude should be between -90 and 90", altitude >= -90.0 && altitude <= 90.0)
        // At the equator on the equinox at noon, the sun should be very close to directly overhead.
        assertTrue("Altitude should be close to 90", altitude > 85.0 && altitude <= 90.0)
    }

     @Test
    fun calculateSunPosition_noExceptionsThrownForValidDates() {
        // Test with various valid dates and locations
        // Primarily to ensure no crashes, rather than specific value validation due to algorithm complexity
        SunCalc.calculateSunPosition(51.5, -0.1, 2023, 1, 15, 10, 30, 0) // Winter
        SunCalc.calculateSunPosition(34.0, -118.2, 2023, 7, 20, 14, 0, -7) // Summer LA
        SunCalc.calculateSunPosition(-33.8, 151.2, 2023, 12, 22, 12, 0, 11) // Summer Sydney (Southern Hemisphere)
        assertTrue(true) // If we reach here, no exceptions were thrown
    }
}
