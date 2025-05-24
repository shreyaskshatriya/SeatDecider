package com.example.bestseatfinder.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    private val DELTA = 1.5 // Allowable difference in degrees for bearing tests

    @Test
    fun calculateBearing_LondonToParis_returnsCorrectBearing() {
        val lat1 = 51.5074 // London
        val lon1 = -0.1278 // London
        val lat2 = 48.8566 // Paris
        val lon2 = 2.3522  // Paris
        val expectedBearing = 156.0 // Approximate bearing

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - London to Paris: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_NewYorkToLosAngeles_returnsCorrectBearing() {
        val lat1 = 40.7128 // New York
        val lon1 = -74.0060 // New York
        val lat2 = 34.0522 // Los Angeles
        val lon2 = -118.2437 // Los Angeles
        val expectedBearing = 265.9 // Approximate bearing

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
         println("GeoUtilsTest - NY to LA: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_PointToEast_returns90Degrees() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 0.0
        val lon2 = 10.0
        val expectedBearing = 90.0

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - Point to East: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_PointToWest_returns270Degrees() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 0.0
        val lon2 = -10.0
        val expectedBearing = 270.0

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - Point to West: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_PointToNorth_returns0Degrees() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 10.0
        val lon2 = 0.0
        val expectedBearing = 0.0

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - Point to North: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_PointToSouth_returns180Degrees() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = -10.0
        val lon2 = 0.0
        val expectedBearing = 180.0

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - Point to South: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }

    @Test
    fun calculateBearing_SamePoints_returns0Degrees() {
        // Or could be undefined/NaN depending on implementation, but 0 is common for no movement
        val lat1 = 40.7128
        val lon1 = -74.0060
        val lat2 = 40.7128
        val lon2 = -74.0060
        val expectedBearing = 0.0 // Bearing to itself

        val resultBearing = GeoUtils.calculateBearing(lat1, lon1, lat2, lon2)
        println("GeoUtilsTest - Same Points: Expected: $expectedBearing, Actual: $resultBearing")
        assertEquals(expectedBearing, resultBearing, DELTA)
    }
}
