package com.oat.clockweather

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.widget.RemoteViews
import java.util.Calendar

class ClockWeatherWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_TICK = "com.oat.clockweather.ACTION_TICK"
        const val ACTION_WEATHER_UPDATED = "com.oat.clockweather.ACTION_WEATHER_UPDATED"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWeatherWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val theme = ThemeManager.getTheme(context, widgetId)
            val weather = WeatherStore.load(context)
            val clockDrawer = ClockDrawer(context)

            // Calculate bitmap size based on widget dimensions
            val options = manager.getAppWidgetOptions(widgetId)
            val dm = context.resources.displayMetrics
            val minW = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110) * dm.density).toInt()
            val minH = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) * dm.density).toInt()

            // Use higher resolution for crisp rendering
            val scale = 2.5f
            val bitmapW = (minW * scale).toInt().coerceAtLeast(280)
            val bitmapH = (minH * scale).toInt().coerceAtLeast(280)

            val bitmap = clockDrawer.drawFull(
                width = bitmapW,
                height = bitmapH,
                theme = theme,
                weather = weather,
                calendar = Calendar.getInstance()
            )

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setImageViewBitmap(R.id.widget_canvas, bitmap)

            manager.updateAppWidget(widgetId, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            updateWidget(context, manager, id)
        }
        scheduleNextMinuteTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TICK, ACTION_WEATHER_UPDATED -> {
                updateAllWidgets(context)
                if (intent.action == ACTION_TICK) {
                    scheduleNextMinuteTick(context)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start weather updates and clock ticking
        WeatherUpdateWorker.schedule(context)
        WeatherUpdateWorker.runOnce(context)
        scheduleNextMinuteTick(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel alarm
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(getTickPendingIntent(context))
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: android.os.Bundle
    ) {
        updateWidget(context, manager, id)
    }

    private fun scheduleNextMinuteTick(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, 1)
        }

        try {
            am.setExact(AlarmManager.RTC, cal.timeInMillis, getTickPendingIntent(context))
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if exact alarms not permitted
            am.set(AlarmManager.RTC, cal.timeInMillis, getTickPendingIntent(context))
        }
    }

    private fun getTickPendingIntent(context: Context): PendingIntent {
        val intent = Intent(ACTION_TICK).apply {
            component = ComponentName(context, ClockWeatherWidget::class.java)
        }
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
