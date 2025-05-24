package com.example.bestseatfinder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bestseatfinder.databinding.ActivityMainBinding
import com.example.bestseatfinder.logic.SeatLogic
import com.example.bestseatfinder.utils.GeoUtils
import com.example.bestseatfinder.utils.SunCalc
import com.example.bestseatfinder.utils.NetworkUtils // Import NetworkUtils
import com.example.bestseatfinder.network.WeatherService
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch // Import launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var selectedYear: Int = -1
    private var selectedMonth: Int = -1
    private var selectedDay: Int = -1
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1

    // Hardcoded city coordinates for placeholder geocoding
    private val cityCoordinates = mapOf(
        "NEW YORK" to (40.7128 to -74.0060),
        "LONDON" to (51.5074 to -0.1278),
        "TOKYO" to (35.6895 to 139.6917),
        "PARIS" to (48.8566 to 2.3522),
        "LOS ANGELES" to (34.0522 to -118.2437),
        "BERLIN" to (52.5200 to 13.4050),
        "MADRID" to (40.4168 to -3.7038)
    )
    private val defaultOriginCoords = cityCoordinates["LONDON"]!!      // London
    private val defaultDestinationCoords = cityCoordinates["PARIS"]!! // Paris

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        setupTimePicker()
        setupFindBestSeatButton()
    }

    private fun setupDatePicker() {
        binding.buttonDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, yearVal, monthVal, dayOfMonthVal ->
                selectedYear = yearVal
                selectedMonth = monthVal // Month is 0-indexed
                selectedDay = dayOfMonthVal
                binding.buttonDatePicker.text = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker() {
        binding.buttonTimePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minuteVal ->
                selectedHour = hourOfDay
                selectedMinute = minuteVal
                binding.buttonTimePicker.text = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            }, hour, minute, true) // true for 24-hour format
            timePickerDialog.show()
        }
    }

    private fun getCoordinatesForCity(cityName: String): Pair<Double, Double>? {
        return cityCoordinates[cityName.uppercase(Locale.getDefault())]
    }

    private fun setupFindBestSeatButton() {
        binding.buttonFindBestSeat.setOnClickListener {
            if (selectedYear == -1 || selectedHour == -1) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val originName = binding.editTextOrigin.text.toString()
            val destinationName = binding.editTextDestination.text.toString()
            val transportMode = binding.spinnerTransportMode.selectedItem.toString()

            var originCoords = getCoordinatesForCity(originName)
            var destCoords = getCoordinatesForCity(destinationName)

            if (originCoords == null || destCoords == null) {
                Toast.makeText(this, "One or both cities not recognized. Using default route (London to Paris).", Toast.LENGTH_LONG).show()
                originCoords = defaultOriginCoords
                destCoords = defaultDestinationCoords
                binding.editTextOrigin.setText("London") // Update UI to reflect default
                binding.editTextDestination.setText("Paris") // Update UI to reflect default
            }

            // Timezone: Assume UTC for now (offset = 0).
            // In a real app, this should be determined based on location and date.
            val timezoneOffsetHours = 0

            try {
                // Calculate sun position at the destination
                // Adding 1 to selectedMonth because Calendar month is 0-indexed, but SunCalc expects 1-indexed.
                val (sunAzimuth, sunAltitude) = SunCalc.calculateSunPosition(
                    latitude = destCoords.first,
                    longitude = destCoords.second,
                    year = selectedYear,
                    month = selectedMonth + 1,
                    day = selectedDay,
                    hour = selectedHour,
                    minute = selectedMinute,
                    timezoneOffsetHours = timezoneOffsetHours
                )

                // Calculate travel bearing from origin to destination
                val travelBearing = GeoUtils.calculateBearing(
                    lat1 = originCoords.first,
                    lon1 = originCoords.second,
                    lat2 = destCoords.first,
                    lon2 = destCoords.second
                )

                // Launch a coroutine to handle weather fetching and then seat recommendation
                lifecycleScope.launch {
                    var weatherConditionMessage = "Weather: Not fetched"
                    var weatherCondition: com.example.bestseatfinder.network.weatherapi.dto.WeatherCondition? = null

                    if (NetworkUtils.isNetworkAvailable(this@MainActivity)) {
                        try {
                            Log.d("WeatherFetch", "Network available. Fetching weather for ${destCoords.first}, ${destCoords.second}")
                            val fetchedWeather = WeatherService.getCurrentWeather(destCoords.first, destCoords.second)
                            if (fetchedWeather != null) {
                                weatherCondition = fetchedWeather
                                weatherConditionMessage = "Weather: ${fetchedWeather.description}, ${fetchedWeather.cloudCoverPercentage}% clouds"
                                Log.d("WeatherFetch", "Success: $weatherConditionMessage")
                            } else {
                                weatherConditionMessage = "Weather: Could not fetch data (API error or empty response)."
                                Log.w("WeatherFetch", "Failed: $weatherConditionMessage")
                            }
                        } catch (e: Exception) {
                            weatherConditionMessage = "Weather: Error fetching data (${e.message})"
                            Log.e("WeatherFetch", "Error: ${e.message}", e)
                        }
                    } else {
                        weatherConditionMessage = "Weather: No internet connection. Data unavailable."
                        Log.w("WeatherFetch", "No internet connection.")
                        Toast.makeText(this@MainActivity, "No internet connection. Weather data unavailable.", Toast.LENGTH_LONG).show()
                    }

                    // Get seat recommendation using weather data and transport mode
                    val recommendation = SeatLogic.getFinalRecommendation(
                        sunAzimuth,
                        travelBearing,
                        weatherCondition, // Pass the fetched weather condition (can be null)
                        transportMode
                    )

                    // Update UI with combined information
                    val resultText = """
                        Recommendation: $recommendation
                        Sun Azimuth: ${"%.1f".format(sunAzimuth)}°
                        Sun Altitude: ${"%.1f".format(sunAltitude)}°
                        Travel Bearing: ${"%.1f".format(travelBearing)}°
                        ($weatherConditionMessage) 
                    """.trimIndent() // weatherConditionMessage provides context from fetch
                    binding.textViewRecommendation.text = resultText
                    Toast.makeText(this@MainActivity, weatherConditionMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error calculating seat: ${e.message}", Toast.LENGTH_LONG).show()
                binding.textViewRecommendation.text = "Error. Could not calculate recommendation."
                Log.e("MainActivity", "Error in FindBestSeat: ", e)
            }
        }
    }
}
