package com.oat.clockweather

import android.content.Context
import android.graphics.*
import java.util.Calendar
import kotlin.math.*

data class WeatherData(
    val temperature: Int = 0,
    val humidity: Int = 0,
    val precipitation: Float = 0f,
    val weatherCode: Int = 0,
    val locationName: String = "—"
)

class ClockDrawer(private val context: Context) {

    private var catBitmap: Bitmap? = null

    private fun getCatBitmap(): Bitmap? {
        if (catBitmap == null) {
            try {
                val resId = context.resources.getIdentifier("cat_face", "drawable", context.packageName)
                if (resId != 0) {
                    catBitmap = BitmapFactory.decodeResource(context.resources, resId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return catBitmap
    }

    fun drawFull(
        width: Int,
        height: Int,
        theme: Theme,
        weather: WeatherData?,
        calendar: Calendar = Calendar.getInstance()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colors = ThemeManager.getColors(theme)

        drawBackground(canvas, width, height, colors)

        val padding = width * 0.065f
        val topRowHeight = height * 0.13f
        val bottomRowHeight = height * 0.12f
        val clockAreaTop = topRowHeight + padding * 0.3f
        val clockAreaBottom = height - bottomRowHeight - padding * 0.3f
        val clockSize = min(clockAreaBottom - clockAreaTop, width - padding * 2)
        val clockCx = width / 2f
        val clockCy = clockAreaTop + (clockAreaBottom - clockAreaTop) / 2f
        val clockTop = clockCy - clockSize / 2f

        // Date centered between widget top and clock top
        drawDateRow(canvas, width.toFloat(), padding, clockTop, colors, calendar)
        // Clock with humidity badge inside
        drawClock(canvas, clockCx, clockCy, clockSize / 2f, colors, calendar, weather)
        drawWeatherRow(canvas, width.toFloat(), height.toFloat(), bottomRowHeight, padding, colors, weather)

        return bitmap
    }

    private fun drawBackground(canvas: Canvas, w: Int, h: Int, colors: ThemeColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        if (colors.bgStart == colors.bgEnd) {
            paint.color = colors.bgStart
        } else {
            paint.shader = LinearGradient(
                w * 0.2f, 0f, w * 0.8f, h.toFloat(),
                colors.bgStart, colors.bgEnd, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, w * 0.13f, h * 0.13f, paint)
    }

    // Date row — centered between widget top and clock top
    private fun drawDateRow(
        canvas: Canvas, w: Float, padding: Float, clockTop: Float,
        colors: ThemeColors, cal: Calendar
    ) {
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)
        val monthName = monthNames[cal.get(Calendar.MONTH)]
        val dateStr = "$dayName, $dayNum $monthName"

        val fontSize = clockTop * 0.50f
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = fontSize
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        // Vertically center in the space between widget top (with padding) and clock top
        val textY = padding + (clockTop - padding) / 2f + fontSize * 0.35f
        canvas.drawText(dateStr, w / 2f, textY, datePaint)
    }

    // Clock — rounded rectangle (chamfered)
    private fun drawClock(
        canvas: Canvas, cx: Float, cy: Float, halfSize: Float,
        colors: ThemeColors, cal: Calendar, weather: WeatherData?
    ) {
        val bezelHalf = halfSize
        val faceHalf = halfSize * 0.93f
        val cornerR = halfSize * 0.22f       // chamfer radius
        val faceCornerR = cornerR * 0.88f

        // === Drop shadow ===
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            setShadowLayer(halfSize * 0.08f, halfSize * 0.02f, halfSize * 0.04f,
                Color.argb(if (colors.isLight) 50 else 130, 0, 0, 0))
        }
        val bezelRect = RectF(cx - bezelHalf, cy - bezelHalf, cx + bezelHalf, cy + bezelHalf)
        canvas.drawRoundRect(bezelRect, cornerR, cornerR, shadowPaint)

        // === Metallic bezel ===
        val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cx - bezelHalf, cy - bezelHalf, cx + bezelHalf, cy + bezelHalf,
                colors.bezelColors, colors.bezelPositions, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(bezelRect, cornerR, cornerR, bezelPaint)

        // Bezel edge highlight
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            color = Color.argb(if (colors.isLight) 25 else 40, 255, 255, 255)
        }
        canvas.drawRoundRect(bezelRect, cornerR, cornerR, edgePaint)

        // === Cat photo face ===
        val faceRect = RectF(cx - faceHalf, cy - faceHalf, cx + faceHalf, cy + faceHalf)
        drawCatFace(canvas, faceRect, faceCornerR, colors)

        // === Humidity badge (top-right inside clock face) ===
        if (weather != null) {
            drawHumidityBadge(canvas, faceRect, faceCornerR, colors, weather.humidity)
        }

        // === Markers ===
        drawMarkers(canvas, cx, cy, faceHalf, colors)

        // === Hands ===
        drawHands(canvas, cx, cy, faceHalf, colors, cal)

        // === Center cap ===
        drawCenterCap(canvas, cx, cy, faceHalf, colors)
    }

    private fun drawHumidityBadge(
        canvas: Canvas, faceRect: RectF, cornerR: Float,
        colors: ThemeColors, humidity: Int
    ) {
        val humStr = "\uD83D\uDCA7$humidity%"
        val fontSize = faceRect.height() * 0.102f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = fontSize
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val textW = textPaint.measureText(humStr)
        val padH = fontSize * 0.5f
        val padV = fontSize * 0.35f
        val badgeX = faceRect.right - textW - padH * 2 - faceRect.width() * 0.06f
        val badgeY = faceRect.top + faceRect.height() * 0.06f

        // Semi-transparent background pill
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(153, 0, 0, 0) // 60% transparent black
        }
        val bgRect = RectF(badgeX, badgeY, badgeX + textW + padH * 2, badgeY + fontSize + padV * 2)
        canvas.drawRoundRect(bgRect, fontSize * 0.5f, fontSize * 0.5f, bgPaint)

        // Text
        canvas.drawText(humStr, badgeX + padH, badgeY + padV + fontSize * 0.85f, textPaint)
    }

    private fun drawCatFace(canvas: Canvas, faceRect: RectF, cornerR: Float, colors: ThemeColors) {
        val w = faceRect.width().toInt()
        val h = faceRect.height().toInt()
        val cx = faceRect.centerX()
        val cy = faceRect.centerY()
        val r = w / 2f

        val catSrc = getCatBitmap()

        if (catSrc != null) {
            val scaled = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(scaled)

            // Center-crop source
            val srcW = catSrc.width.toFloat()
            val srcH = catSrc.height.toFloat()
            val cropSize = min(srcW, srcH)
            val srcR = Rect(
                ((srcW - cropSize) / 2f).toInt(), ((srcH - cropSize) / 2f).toInt(),
                ((srcW + cropSize) / 2f).toInt(), ((srcH + cropSize) / 2f).toInt()
            )

            // Clip to rounded rect
            val clipPath = Path().apply {
                addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), cornerR, cornerR, Path.Direction.CW)
            }
            c.clipPath(clipPath)

            // Draw cat photo
            c.drawBitmap(catSrc, srcR, Rect(0, 0, w, h),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

            // Vignette (very subtle, just darken extreme edges)
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    w / 2f, h / 2f, r,
                    intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(15, 0, 0, 0), Color.argb(35, 0, 0, 0)),
                    floatArrayOf(0f, 0.8f, 0.95f, 1f), Shader.TileMode.CLAMP
                )
            }
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignettePaint)

            canvas.drawBitmap(scaled, faceRect.left, faceRect.top, Paint(Paint.ANTI_ALIAS_FLAG))
            scaled.recycle()
        } else {
            // Fallback gradient face
            val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx - r * 0.2f, cy - r * 0.25f, r * 1.3f,
                    colors.faceColors, colors.facePositions, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(faceRect, cornerR, cornerR, facePaint)
        }

        // Face inner edge
        val faceEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1f
            color = Color.argb(if (colors.isLight) 20 else 30, 0, 0, 0)
        }
        canvas.drawRoundRect(faceRect, cornerR, cornerR, faceEdgePaint)
    }

    private fun drawMarkers(canvas: Canvas, cx: Float, cy: Float, r: Float, colors: ThemeColors) {
        val markerR = r * 0.85f
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.accentColor }
        val angle12 = Math.toRadians(-90.0)
        canvas.drawCircle(
            cx + (markerR * cos(angle12)).toFloat(),
            cy + (markerR * sin(angle12)).toFloat(),
            r * 0.04f, accentPaint
        )
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(80, 255, 255, 255) }
        for (i in intArrayOf(3, 6, 9)) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            canvas.drawCircle(
                cx + (markerR * cos(angle)).toFloat(),
                cy + (markerR * sin(angle)).toFloat(),
                r * 0.03f, dotPaint
            )
        }
    }

    private fun drawHands(
        canvas: Canvas, cx: Float, cy: Float, r: Float,
        colors: ThemeColors, cal: Calendar
    ) {
        val hours = cal.get(Calendar.HOUR)
        val minutes = cal.get(Calendar.MINUTE)
        val hourAngle = Math.toRadians(((hours + minutes / 60.0) * 30 - 90))
        val minuteAngle = Math.toRadians((minutes * 6 - 90).toDouble())
        val handCy = cy + r * 0.035f

        // Hour hand
        drawHandLine(canvas, cx + 1, handCy + 2, hourAngle, r * 0.52f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(65, 0, 0, 0); strokeWidth = r * 0.13f; strokeCap = Paint.Cap.ROUND })
        drawHandLine(canvas, cx, handCy, hourAngle, r * 0.52f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.handStrokeColor; strokeWidth = r * 0.12f; strokeCap = Paint.Cap.ROUND })
        drawHandLine(canvas, cx, handCy, hourAngle, r * 0.52f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.handLumeColor; strokeWidth = r * 0.09f; strokeCap = Paint.Cap.ROUND })

        // Minute hand (bigger)
        drawHandLine(canvas, cx + 1, handCy + 2, minuteAngle, r * 0.75f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(50, 0, 0, 0); strokeWidth = r * 0.09f; strokeCap = Paint.Cap.ROUND })
        drawHandLine(canvas, cx, handCy, minuteAngle, r * 0.75f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.handStrokeColor; strokeWidth = r * 0.08f; strokeCap = Paint.Cap.ROUND })
        drawHandLine(canvas, cx, handCy, minuteAngle, r * 0.75f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.handLumeColor; strokeWidth = r * 0.055f; strokeCap = Paint.Cap.ROUND })
    }

    private fun drawHandLine(canvas: Canvas, cx: Float, cy: Float, angle: Double, length: Float, paint: Paint) {
        canvas.drawLine(cx, cy, cx + (length * cos(angle)).toFloat(), cy + (length * sin(angle)).toFloat(), paint)
    }

    private fun drawCenterCap(canvas: Canvas, cx: Float, cy: Float, r: Float, colors: ThemeColors) {
        val handCy = cy + r * 0.035f
        canvas.drawCircle(cx, handCy, r * 0.09f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (colors.isLight) 0xFFD0D0D0.toInt() else 0xFFB0B0B0.toInt() })
        canvas.drawCircle(cx, handCy, r * 0.065f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (colors.isLight) 0xFFE8E8E8.toInt() else 0xFFD8D8D8.toInt() })
        canvas.drawCircle(cx, handCy, r * 0.035f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF888888.toInt() })
        canvas.drawCircle(cx - r * 0.02f, handCy - r * 0.02f, r * 0.02f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((255 * colors.specularAlpha * 0.6f).toInt(), 255, 255, 255) })
    }

    private fun drawWeatherRow(
        canvas: Canvas, w: Float, h: Float, rowH: Float,
        padding: Float, colors: ThemeColors, weather: WeatherData?
    ) {
        val y = h - rowH * 0.4f
        val iconStr = getWeatherEmoji(weather?.weatherCode ?: -1)
        val tempStr = if (weather != null) "${weather.temperature}°C" else "—"

        val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (colors.isLight) colors.textPrimary
                    else if (ThemeManager.getColors(Theme.AMOLED) == colors) colors.accentColor
                    else Color.WHITE
            textSize = rowH * 0.78f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = rowH * 0.8f }
        val precipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (colors.isLight) colors.textSecondary else colors.textPrimary
            textSize = rowH * 0.6f
        }

        val precipStr = if (weather != null) "\u2614${String.format("%.1f", weather.precipitation)}mm" else ""
        val iconW = iconPaint.measureText(iconStr)
        val tempW = tempPaint.measureText(tempStr)
        val precipW = if (weather != null) precipPaint.measureText(precipStr) else 0f
        val gap = w * 0.025f
        val totalW = iconW + gap + tempW + (if (weather != null) gap * 1.5f + precipW else 0f)
        var x = (w - totalW) / 2f

        canvas.drawText(iconStr, x, y, iconPaint); x += iconW + gap
        canvas.drawText(tempStr, x, y, tempPaint); x += tempW + gap * 1.5f
        if (weather != null) {
            canvas.drawText(precipStr, x, y, precipPaint)
        }
    }

    private fun getWeatherEmoji(code: Int): String = when (code) {
        0 -> "☀️"; 1, 2 -> "🌤️"; 3 -> "☁️"
        45, 48 -> "🌫️"; in 51..67 -> "🌧️"
        in 71..77 -> "❄️"; in 80..82 -> "🌦️"
        in 95..99 -> "⛈️"; else -> "🌡️"
    }
}
