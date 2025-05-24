package com.example.bestseatfinder.utils

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Calculates the position of the sun (azimuth and altitude) for a given location and time.
 * The formulas used are simplified and provide an approximation.
 * For higher accuracy, a more complex algorithm or library (e.g., NREL's SPA) would be needed.
 *
 * Sources:
 * - General formulas for solar position calculation.
 * - Inspired by concepts from Jean Meeus "Astronomical Algorithms" and similar simplified models.
 * - Online resources and calculators for solar position components.
 */
object SunCalc {

    /**
     * Calculates the sun's azimuth and altitude.
     *
     * @param latitude Latitude of the observer in degrees.
     * @param longitude Longitude of the observer in degrees.
     * @param year The year.
     * @param month The month (1-12).
     * @param day The day of the month.
     * @param hour The hour of the day (0-23) in the local timezone.
     * @param minute The minute of the hour.
     * @param timezoneOffsetHours The timezone offset from UTC in hours (e.g., -5 for EST, +1 for CET).
     * @return Pair<Double, Double> - Azimuth (degrees from North, 0-360) and Altitude (degrees above horizon, -90 to 90).
     *         Azimuth: 0 = North, 90 = East, 180 = South, 270 = West.
     */
    fun calculateSunPosition(
        latitude: Double,
        longitude: Double,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timezoneOffsetHours: Int
    ): Pair<Double, Double> {

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(year, month - 1, day, hour, minute, 0)
        // Adjust for local timezone to get UTC
        calendar.add(Calendar.HOUR_OF_DAY, -timezoneOffsetHours)

        val jDay = calculateJulianDay(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )

        val n = jDay - 2451545.0 // Days since Jan 1, 2000 12:00 UT
        val omega = 2.1429 - 0.0010394594 * n // Longitude of ascending node
        var L = 4.8950630 + 0.017202791698 * n // Mean longitude of the Sun
        val M = 6.2400600 + 0.0172019699 * n // Mean anomaly of the Sun

        // Ecliptic longitude of the Sun
        val eclipticLongitude = L + 0.03341607 * sin(M) + 0.00034894 * sin(2 * M) - 0.0001134 * sin(omega) - 0.0000203 * sin(L - omega) // Simplified
        // Obliquity of the ecliptic
        val obliquity = 0.4090928 - 6.2140e-9 * n + 0.0000396 * cos(omega) // Simplified

        // Right Ascension (RA) and Declination (Dec)
        val alpha = atan2(cos(obliquity) * sin(eclipticLongitude), cos(eclipticLongitude))
        val delta = asin(sin(obliquity) * sin(eclipticLongitude))

        // Local Sidereal Time (LST)
        val gmst0 = (L + PI) % (2 * PI) / (2 * PI) * 24 // Approximate GMST at 0h UT
        val siderealTime = gmst0 + (calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE)/60.0) * 1.00273790935 + longitude / 15.0
        val localHourAngleRad = (siderealTime * 15.0 * (PI / 180.0)) - alpha

        val latRad = Math.toRadians(latitude)

        // Altitude
        val sinAltitude = sin(latRad) * sin(delta) + cos(latRad) * cos(delta) * cos(localHourAngleRad)
        val altitude = Math.toDegrees(asin(sinAltitude))

        // Azimuth
        val cosAzimuth = (sin(delta) - sin(latRad) * sinAltitude) / (cos(latRad) * cos(asin(sinAltitude)))
        var azimuth = Math.toDegrees(acos(max(-1.0, min(1.0,cosAzimuth)))) // Clamp to avoid NaN due to precision

        if (sin(localHourAngleRad) > 0) {
            azimuth = 360.0 - azimuth
        }
        // Azimuth: 0 = North, 90 = East, 180 = South, 270 = West
        // The formula above calculates azimuth from South. Convert to North-based.
        azimuth = (azimuth + 180) % 360

        return Pair(azimuth, altitude)
    }

    /**
     * Calculates Julian Day from calendar date.
     * Algorithm from "Astronomical Algorithms" by Jean Meeus, Chapter 7.
     */
    private fun calculateJulianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Double {
        var y = year
        var m = month
        if (month <= 2) {
            y -= 1
            m += 12
        }
        val A = floor(y / 100.0)
        val B = 2 - A + floor(A / 4.0)
        val dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + dayFraction + B - 1524.5
    }
}
