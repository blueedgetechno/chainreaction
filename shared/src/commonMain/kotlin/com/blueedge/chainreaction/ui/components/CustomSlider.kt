package com.blueedge.chainreaction.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A discrete slider that snaps to [valueCount] evenly-spaced stops.
 *
 * @param value        Current selected index (0-based, must be in 0 until [valueCount]).
 * @param onValueChange Called with the new index when the user drags or taps.
 * @param valueCount   Total number of discrete stops.
 * @param modifier     Modifier applied to the slider container.
 * @param trackHeight  Height of the track bar.
 * @param thumbRadius  Radius of the draggable thumb circle.
 * @param activeColor  Color for the filled portion of the track and the thumb.
 * @param inactiveColor Color for the unfilled portion of the track.
 * @param stepDotRadius Radius of the small step-indicator dots on the track (0 to hide).
 */
@Composable
fun CustomSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueCount: Int,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 16.dp,
    thumbRadius: Dp = 14.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    stepDotRadius: Dp = 3.5.dp
) {
    require(valueCount >= 2) { "valueCount must be >= 2" }
    val coercedValue = value.coerceIn(0, valueCount - 1)

    val density = LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val stepDotRadiusPx = with(density) { stepDotRadius.toPx() }

    // Mutable drag position so thumb follows the finger smoothly before snapping.
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(coercedValue.toFloat() / (valueCount - 1)) }

    // Keep drag fraction in sync when value changes externally and not dragging.
    if (!isDragging) {
        dragFraction = coercedValue.toFloat() / (valueCount - 1)
    }

    val totalHeight = thumbRadius * 2  // canvas height encompasses the thumb

    Box(
        modifier = modifier.height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .pointerInput(valueCount) {
                    detectTapGestures { offset ->
                        val inset = trackHeightPx / 2
                        val travelWidth = size.width - 2 * inset
                        val fraction =
                            ((offset.x - inset) / travelWidth).coerceIn(0f, 1f)
                        val newIndex =
                            (fraction * (valueCount - 1)).roundToInt().coerceIn(0, valueCount - 1)
                        dragFraction = newIndex.toFloat() / (valueCount - 1)
                        onValueChange(newIndex)
                    }
                }
                .pointerInput(valueCount) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val inset = trackHeightPx / 2
                            val travelWidth = size.width - 2 * inset
                            dragFraction =
                                ((offset.x - inset) / travelWidth).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val inset = trackHeightPx / 2
                            val travelWidth = size.width - 2 * inset
                            dragFraction =
                                ((change.position.x - inset) / travelWidth).coerceIn(0f, 1f)
                            val newIndex =
                                (dragFraction * (valueCount - 1)).roundToInt()
                                    .coerceIn(0, valueCount - 1)
                            onValueChange(newIndex)
                        },
                        onDragEnd = {
                            // Snap
                            val snappedIndex =
                                (dragFraction * (valueCount - 1)).roundToInt()
                                    .coerceIn(0, valueCount - 1)
                            dragFraction = snappedIndex.toFloat() / (valueCount - 1)
                            isDragging = false
                            onValueChange(snappedIndex)
                        },
                        onDragCancel = {
                            isDragging = false
                            dragFraction = coercedValue.toFloat() / (valueCount - 1)
                        }
                    )
                }
        ) {
            val trackStartX = 0f
            val trackWidth = size.width
            val centerY = size.height / 2

            // ── Inactive track (full width) ──
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(trackStartX, centerY - trackHeightPx / 2),
                size = Size(trackWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2)
            )

            // ── Active track (up to thumb) ──
            val dotInset = trackHeightPx / 2
            val thumbX = dotInset + (trackWidth - 2 * dotInset) * dragFraction
            val activeWidth = thumbX
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(trackStartX, centerY - trackHeightPx / 2),
                    size = Size(activeWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2)
                )
            }

            // ── Step dots (inset so they don't leak outside rounded track) ──
            if (stepDotRadiusPx > 0f) {
                val dotStartX = trackStartX + dotInset
                val dotEndX = trackStartX + trackWidth - dotInset
                val dotSpan = dotEndX - dotStartX
                for (i in 0 until valueCount) {
                    val dotX = dotStartX + dotSpan * i / (valueCount - 1)
                    // Skip dots hidden behind the thumb
                    if (kotlin.math.abs(dotX - thumbX) < thumbRadiusPx * 0.8f) continue
                    val isOnActivePortion = dotX <= thumbX
                    drawCircle(
                        color = if (isOnActivePortion) Color.White else activeColor,
                        radius = stepDotRadiusPx,
                        center = Offset(dotX, centerY)
                    )
                }
            }

            // ── Thumb ──
            drawCircle(
                color = activeColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, centerY)
            )
            // Inner white dot for contrast
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx * 0.45f,
                center = Offset(thumbX, centerY)
            )
        }
    }
}
