package com.oat.clockweather

import android.graphics.Color

enum class Theme(val id: Int, val label: String) {
    DEEP_OCEAN(0, "Deep Ocean"),
    SUNSET(1, "Sunset"),
    FROSTED(2, "Frosted Glass"),
    AMOLED(3, "AMOLED"),
    CLEAN_WHITE(4, "Clean White");

    companion object {
        fun fromId(id: Int) = entries.firstOrNull { it.id == id } ?: DEEP_OCEAN
    }
}

data class ThemeColors(
    // Widget background
    val bgStart: Int,
    val bgEnd: Int,
    val bgAngle: Float,
    // Clock bezel gradient (6 stops)
    val bezelColors: IntArray,
    val bezelPositions: FloatArray,
    // Clock face gradient (5 stops, radial)
    val faceColors: IntArray,
    val facePositions: FloatArray,
    // Markers & hands
    val markerColor: Int,
    val accentColor: Int,     // 12 o'clock triangle + second hand
    val handLumeColor: Int,   // hour/minute hand fill
    val handStrokeColor: Int, // hour/minute hand outline
    // Text colors
    val textPrimary: Int,
    val textSecondary: Int,
    val textTertiary: Int,
    // Specular highlight intensity
    val specularAlpha: Float,
    // Is light theme?
    val isLight: Boolean
)

object ThemeManager {

    private const val PREFS = "clock_weather_prefs"
    private const val KEY_THEME = "theme_id"

    fun getTheme(context: android.content.Context, widgetId: Int): Theme {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        return Theme.fromId(prefs.getInt("${KEY_THEME}_$widgetId", 0))
    }

    fun setTheme(context: android.content.Context, widgetId: Int, theme: Theme) {
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .edit().putInt("${KEY_THEME}_$widgetId", theme.id).apply()
    }

    fun getColors(theme: Theme): ThemeColors = when (theme) {
        Theme.DEEP_OCEAN -> ThemeColors(
            bgStart = 0xFF1A1A2E.toInt(), bgEnd = 0xFF0F3460.toInt(), bgAngle = 145f,
            bezelColors = intArrayOf(0xFFE8E8E8.toInt(), 0xFFF8F8F8.toInt(), 0xFFB0B0B0.toInt(), 0xFFDADADA.toInt(), 0xFFA0A0A0.toInt(), 0xFF888888.toInt()),
            bezelPositions = floatArrayOf(0f, 0.15f, 0.3f, 0.5f, 0.7f, 1f),
            faceColors = intArrayOf(0xFFEEEEEE.toInt(), 0xFFD8D8D8.toInt(), 0xFFB8B8B8.toInt(), 0xFF909090.toInt(), 0xFF787878.toInt()),
            facePositions = floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            markerColor = 0xFF666666.toInt(),
            accentColor = 0xFFFF6B4A.toInt(),
            handLumeColor = 0xFFE8E0C8.toInt(),
            handStrokeColor = 0xFF444444.toInt(),
            textPrimary = 0xF2FFFFFF.toInt(),
            textSecondary = 0x73FFFFFF.toInt(),
            textTertiary = 0x59FFFFFF.toInt(),
            specularAlpha = 0.55f,
            isLight = false
        )

        Theme.SUNSET -> ThemeColors(
            bgStart = 0xFF2D1B4E.toInt(), bgEnd = 0xFF6B2737.toInt(), bgAngle = 145f,
            bezelColors = intArrayOf(0xFFD8C8E0.toInt(), 0xFFF0E8F4.toInt(), 0xFFA898B0.toInt(), 0xFFD0C0D8.toInt(), 0xFF9888A0.toInt(), 0xFFC0B0C8.toInt()),
            bezelPositions = floatArrayOf(0f, 0.15f, 0.3f, 0.5f, 0.7f, 1f),
            faceColors = intArrayOf(0xFFE8E0EE.toInt(), 0xFFD0C0D8.toInt(), 0xFFB0A0B8.toInt(), 0xFF887888.toInt(), 0xFF706070.toInt()),
            facePositions = floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            markerColor = 0xFF5A4A5A.toInt(),
            accentColor = 0xFFFF8855.toInt(),
            handLumeColor = 0xFFE8DDD0.toInt(),
            handStrokeColor = 0xFF3A2A3A.toInt(),
            textPrimary = 0xF2FFFFFF.toInt(),
            textSecondary = 0x73FFFFFF.toInt(),
            textTertiary = 0x59FFFFFF.toInt(),
            specularAlpha = 0.5f,
            isLight = false
        )

        Theme.FROSTED -> ThemeColors(
            bgStart = 0x26A0B8C8.toInt(), bgEnd = 0x26A0B8C8.toInt(), bgAngle = 0f,
            bezelColors = intArrayOf(0xFFE8E8E8.toInt(), 0xFFFAFAFA.toInt(), 0xFFC0C0C0.toInt(), 0xFFF0F0F0.toInt(), 0xFFA8A8A8.toInt(), 0xFFA0A0A0.toInt()),
            bezelPositions = floatArrayOf(0f, 0.15f, 0.3f, 0.5f, 0.7f, 1f),
            faceColors = intArrayOf(0xFFF8F8F8.toInt(), 0xFFE8E8E8.toInt(), 0xFFC8C8C8.toInt(), 0xFFA8A8A8.toInt(), 0xFF909090.toInt()),
            facePositions = floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            markerColor = 0xFF777777.toInt(),
            accentColor = 0xFFE05030.toInt(),
            handLumeColor = 0xFFD0C8B8.toInt(),
            handStrokeColor = 0xFF555555.toInt(),
            textPrimary = 0xFF222222.toInt(),
            textSecondary = 0xFF777777.toInt(),
            textTertiary = 0xFFAAAAAA.toInt(),
            specularAlpha = 0.7f,
            isLight = true
        )

        Theme.AMOLED -> ThemeColors(
            bgStart = 0xFF000000.toInt(), bgEnd = 0xFF000000.toInt(), bgAngle = 0f,
            bezelColors = intArrayOf(0xFF555555.toInt(), 0xFF777777.toInt(), 0xFF333333.toInt(), 0xFF666666.toInt(), 0xFF2A2A2A.toInt(), 0xFF2A2A2A.toInt()),
            bezelPositions = floatArrayOf(0f, 0.15f, 0.3f, 0.5f, 0.7f, 1f),
            faceColors = intArrayOf(0xFF505050.toInt(), 0xFF404040.toInt(), 0xFF2A2A2A.toInt(), 0xFF1A1A1A.toInt(), 0xFF111111.toInt()),
            facePositions = floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            markerColor = 0xFF3A3A3A.toInt(),
            accentColor = 0xFF00FF88.toInt(),
            handLumeColor = 0xFFC0C0B8.toInt(),
            handStrokeColor = 0xFF1A1A1A.toInt(),
            textPrimary = 0xE6FFFFFF.toInt(),
            textSecondary = 0x59FFFFFF.toInt(),
            textTertiary = 0x40FFFFFF.toInt(),
            specularAlpha = 0.2f,
            isLight = false
        )

        Theme.CLEAN_WHITE -> ThemeColors(
            bgStart = 0xF5FFFFFF.toInt(), bgEnd = 0xF5FFFFFF.toInt(), bgAngle = 0f,
            bezelColors = intArrayOf(0xFFF0F0F0.toInt(), 0xFFFCFCFC.toInt(), 0xFFD0D0D0.toInt(), 0xFFF5F5F5.toInt(), 0xFFC0C0C0.toInt(), 0xFFC0C0C0.toInt()),
            bezelPositions = floatArrayOf(0f, 0.15f, 0.3f, 0.5f, 0.7f, 1f),
            faceColors = intArrayOf(0xFFFFFFFF.toInt(), 0xFFF5F5F5.toInt(), 0xFFE0E0E0.toInt(), 0xFFC8C8C8.toInt(), 0xFFB0B0B0.toInt()),
            facePositions = floatArrayOf(0f, 0.3f, 0.6f, 0.85f, 1f),
            markerColor = 0xFF999999.toInt(),
            accentColor = 0xFFE05030.toInt(),
            handLumeColor = 0xFF444444.toInt(),
            handStrokeColor = 0xFF777777.toInt(),
            textPrimary = 0xFF222222.toInt(),
            textSecondary = 0xFF777777.toInt(),
            textTertiary = 0xFFAAAAAA.toInt(),
            specularAlpha = 0.9f,
            isLight = true
        )
    }
}
