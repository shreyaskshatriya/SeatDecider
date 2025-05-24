package com.example.bestseatfinder.utils

import kotlin.math.*

object GeoUtils {

    /**
     * Calculates the initial bearing between two points on Earth.
     *
     * Formula from: https://www.movable-type.co.uk/scripts/latlong.html (section "Bearing")
     * θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ )
     * where φ1,λ1 is the start point, φ2,λ2 the end point (Δλ is the difference in longitude)
     *
     * @param lat1 Latitude of the first point in degrees.
     * @param lon1 Longitude of the first point in degrees.
     * @param lat2 Latitude of the second point in degrees.
     * @param lon2 Longitude of the second point in degrees.
     * @return Initial bearing in degrees (0-360, 0 = North).
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val deltaLonRad = lon2Rad - lon1Rad

        val y = sin(deltaLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)
        
        val bearingRad = atan2(y, x)
        
        // Convert bearing from radians to degrees and normalize to 0-360 range
        return (Math.toDegrees(bearingRad) + 360) % 360
    }
}
