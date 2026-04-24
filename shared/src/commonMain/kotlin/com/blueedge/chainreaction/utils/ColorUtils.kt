package com.blueedge.chainreaction.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Pure-Kotlin HSV color manipulation (replaces android.graphics.Color). */
object ColorUtils {
    /**
     * Given a Compose [Color], reduce saturation and push brightness up
     * to produce a lighter, less saturated version for cell backgrounds.
     */
    fun lightenColor(color: Color): Color {
        val r = color.red
        val g = color.green
        val b = color.blue

        val cMax = max(r, max(g, b))
        val cMin = min(r, min(g, b))
        val delta = cMax - cMin

        // Hue
        val hue = when {
            delta == 0f -> 0f
            cMax == r -> 60f * (((g - b) / delta) % 6f)
            cMax == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }

        // Saturation
        val saturation = if (cMax == 0f) 0f else delta / cMax

        // Value
        val value = cMax

        // Apply modifications: reduce saturation, push brightness up
        val newSat = (saturation * 0.42f).coerceIn(0f, 1f)
        val newVal = (value + (1f - value) * 0.75f).coerceIn(0f, 1f)

        // HSV to RGB
        return hsvToColor(hue, newSat, newVal, color.alpha)
    }

    private fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float): Color {
        val c = v * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = v - c

        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = r1 + m,
            green = g1 + m,
            blue = b1 + m,
            alpha = alpha
        )
    }
}
