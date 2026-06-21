package com.oat.clockweather

import android.content.Context

object WeatherStore {

    private const val PREFS = "weather_cache"

    fun save(context: Context, data: WeatherData) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putInt("temperature", data.temperature)
            putInt("humidity", data.humidity)
            putFloat("precipitation", data.precipitation)
            putInt("weather_code", data.weatherCode)
            putString("location_name", data.locationName)
            putLong("updated_at", System.currentTimeMillis())
            apply()
        }
    }

    fun load(context: Context): WeatherData? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updatedAt = prefs.getLong("updated_at", 0)
        if (updatedAt == 0L) return null

        return WeatherData(
            temperature = prefs.getInt("temperature", 0),
            humidity = prefs.getInt("humidity", 0),
            precipitation = prefs.getFloat("precipitation", 0f),
            weatherCode = prefs.getInt("weather_code", 0),
            locationName = prefs.getString("location_name", "—") ?: "—"
        )
    }

    fun saveLocation(context: Context, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putFloat("lat", lat.toFloat())
            putFloat("lon", lon.toFloat())
            apply()
        }
    }

    fun getLocation(context: Context): Pair<Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lat = prefs.getFloat("lat", Float.MIN_VALUE)
        val lon = prefs.getFloat("lon", Float.MIN_VALUE)
        return if (lat != Float.MIN_VALUE) Pair(lat.toDouble(), lon.toDouble()) else null
    }

    fun getLocationName(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("location_name", "—") ?: "—"
    }
}
