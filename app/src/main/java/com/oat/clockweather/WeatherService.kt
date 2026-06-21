package com.oat.clockweather

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object WeatherService {

    fun fetchWeather(lat: Double, lon: Double): WeatherData? {
        return try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,weather_code,precipitation" +
                "&timezone=auto"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                conn.disconnect()
                parseResponse(response)
            } else {
                conn.disconnect()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseResponse(json: String): WeatherData? {
        return try {
            val obj = JSONObject(json)
            val current = obj.getJSONObject("current")
            WeatherData(
                temperature = current.getDouble("temperature_2m").toInt(),
                humidity = current.getInt("relative_humidity_2m"),
                precipitation = current.optDouble("precipitation", 0.0).toFloat(),
                weatherCode = current.getInt("weather_code"),
                locationName = "" // Will be filled by reverse geocoder
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun reverseGeocode(lat: Double, lon: Double): String {
        return try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon&current=temperature_2m&timezone=auto"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                conn.disconnect()
                val obj = JSONObject(response)
                obj.optString("timezone", "Unknown")
                    .substringAfterLast("/")
                    .replace("_", " ")
            } else {
                conn.disconnect()
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
