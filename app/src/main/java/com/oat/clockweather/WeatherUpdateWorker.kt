package com.oat.clockweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get location
            val location = LocationHelper.getLocation(context)
            val lat: Double
            val lon: Double

            if (location != null) {
                lat = location.latitude
                lon = location.longitude
                WeatherStore.saveLocation(context, lat, lon)
            } else {
                // Fallback to cached location
                val cached = WeatherStore.getLocation(context) ?: return Result.retry()
                lat = cached.first
                lon = cached.second
            }

            // Fetch weather
            val weather = WeatherService.fetchWeather(lat, lon) ?: return Result.retry()

            // Get city name
            val cityName = LocationHelper.getCityName(context, lat, lon)
            val weatherWithCity = weather.copy(locationName = cityName)

            // Cache
            WeatherStore.save(context, weatherWithCity)

            // Notify widget to redraw
            val intent = Intent(ClockWeatherWidget.ACTION_WEATHER_UPDATED)
            intent.component = ComponentName(context, ClockWeatherWidget::class.java)
            context.sendBroadcast(intent)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "weather_update"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                30, TimeUnit.MINUTES,
                15, TimeUnit.MINUTES  // flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runOnce(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<WeatherUpdateWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
