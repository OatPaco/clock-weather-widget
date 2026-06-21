package com.oat.clockweather

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Calendar

class ConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Default result = cancelled
        setResult(RESULT_CANCELED)

        // Get widget ID
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_config)

        val grid = findViewById<LinearLayout>(R.id.theme_grid)
        val clockDrawer = ClockDrawer(this)

        // Create theme previews
        val themes = Theme.entries
        var row: LinearLayout? = null

        for ((index, theme) in themes.withIndex()) {
            if (index % 3 == 0) {
                row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 16 }
                }
                grid.addView(row)
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 12
                    marginEnd = 12
                }
            }

            // Preview image
            val previewSize = (resources.displayMetrics.density * 90).toInt()
            val preview = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(previewSize, previewSize)
                scaleType = ImageView.ScaleType.FIT_CENTER

                val sampleWeather = WeatherData(32, 65, 0, "Bangkok")
                val bitmap = clockDrawer.drawFull(
                    previewSize * 2, previewSize * 2,
                    theme, sampleWeather, Calendar.getInstance()
                )
                setImageBitmap(bitmap)
            }

            // Label
            val label = TextView(this).apply {
                text = theme.label
                setTextColor(Color.WHITE)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }

            container.addView(preview)
            container.addView(label)

            container.setOnClickListener {
                selectTheme(theme)
            }

            row?.addView(container)
        }
    }

    private fun selectTheme(theme: Theme) {
        ThemeManager.setTheme(this, widgetId, theme)

        // Start weather updates
        WeatherUpdateWorker.schedule(this)
        WeatherUpdateWorker.runOnce(this)

        // Update widget immediately
        val manager = AppWidgetManager.getInstance(this)
        ClockWeatherWidget.updateWidget(this, manager, widgetId)

        // Return success
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}
