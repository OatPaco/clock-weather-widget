package com.oat.clockweather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WeatherUpdateWorker.schedule(context)
            ClockWeatherWidget.updateAllWidgets(context)
        }
    }
}
