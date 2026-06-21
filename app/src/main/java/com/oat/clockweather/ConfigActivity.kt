package com.oat.clockweather

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.content.ContextCompat
import java.util.Calendar

class ConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingTheme: Theme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

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
                ).apply { marginStart = 12; marginEnd = 12 }
            }

            val previewSize = (resources.displayMetrics.density * 90).toInt()
            val preview = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(previewSize, previewSize)
                scaleType = ImageView.ScaleType.FIT_CENTER
                val sampleWeather = WeatherData(32, 65, 0f, 0, "Bangkok")
                setImageBitmap(clockDrawer.drawFull(
                    previewSize * 2, previewSize * 2,
                    theme, sampleWeather, Calendar.getInstance()
                ))
            }

            val label = TextView(this).apply {
                text = theme.label
                setTextColor(Color.WHITE)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }

            container.addView(preview)
            container.addView(label)
            container.setOnClickListener { selectTheme(theme) }
            row?.addView(container)
        }
    }

    private fun selectTheme(theme: Theme) {
        ThemeManager.setTheme(this, widgetId, theme)
        pendingTheme = theme

        // Request location permission before fetching weather
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        } else {
            finishSetup()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Finish setup regardless of whether permission was granted
        finishSetup()
    }

    private fun finishSetup() {
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
