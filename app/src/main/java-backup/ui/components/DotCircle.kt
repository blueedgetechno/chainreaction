package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun DotCircle(
    dotCount: Int,
    color: Color,
    isCurrentPlayer: Boolean,
    isExploding: Boolean,
    previousDots: Int = 0,
    modifier: Modifier = Modifier
) {
    // Animation progress from 0 (previous positions) to 1 (final positions)
    // Initialize to 0 if animation needed, to avoid snap-then-animate flicker
    // previousDots == -1 means explosion arrival on empty cell: animate positions, skip fade
    val needsAnimation = previousDots != dotCount
    val animProgress = remember(dotCount, previousDots) {
        Animatable(if (needsAnimation) 0f else 1f)
    }

    // Separate fade-in alpha for 0→1 transition (dot appearing on empty cell via user tap)
    // Skip fade for explosion arrivals (-1) since movement overlay handles the visual
    val isFadeIn = previousDots == 0 && dotCount > 0
    val fadeAlpha = remember(dotCount, previousDots) {
        Animatable(if (isFadeIn) 0f else 1f)
    }

    LaunchedEffect(dotCount) {
        if (needsAnimation) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
        }
    }

    LaunchedEffect(dotCount, previousDots) {
        if (isFadeIn) {
            fadeAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
        }
    }

    val progress = animProgress.value
    val alpha = fadeAlpha.value

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val circleRadius = size.minDimension * 0.38f
        val center = Offset(size.width / 2, size.height / 2)

        // Draw main circle
        drawCircle(
            color = color,
            radius = circleRadius,
            center = center
        )

        // Draw white dots with animated positions (fade-in for 0→N)
        val dotRadius = circleRadius * 0.2f
        val spread = circleRadius * 0.45f
        val animatedPositions = getAnimatedDotPositions(
            previousCount = previousDots,
            targetCount = dotCount,
            center = center,
            spread = spread,
            progress = progress
        )

        for (pos in animatedPositions) {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = dotRadius,
                center = pos
            )
        }
    }
}

/**
 * Computes animated dot positions that transition from the previous layout to the new layout.
 *
 * Transition rules:
 * - 0→N: All dots emerge from center (fade-in handled separately)
 * - 1→2: One dot splits from center into two horizontal positions
 * - 1→4: Center dot splits into 4 corners
 * - 1→5: Center dot splits into 4 corners + center stays
 * - 2→3: Third dot emerges from center; existing two slide down to triangle
 * - 2→4: Each horizontal dot splits vertically into 2 corners
 * - 2→5: Each horizontal dot splits to corners + center emerges
 * - 2→6: Each horizontal dot splits into a full column of 3
 * - 3→4: Triangle bottom→square bottom, triangle top splits into square top edge
 * - 3→5: Triangle bottom→square bottom, top splits to top corners, center emerges
 * - 3→6: Triangle maps to die-6 layout with middle dots emerging from center
 * - 3→7: Triangle maps to hexagon with top/sides staying, remaining emerge from center
 */
private fun getAnimatedDotPositions(
    previousCount: Int,
    targetCount: Int,
    center: Offset,
    spread: Float,
    progress: Float
): List<Offset> {
    // Target positions for each count
    val target = getDotPositions(targetCount, center, spread)

    if (progress >= 1f || previousCount == targetCount) {
        return target
    }

    fun lerp(from: Offset, to: Offset): Offset = Offset(
        x = from.x + (to.x - from.x) * progress,
        y = from.y + (to.y - from.y) * progress
    )

    return when {
        // 0→N or -1→N (explosion arrival on empty cell): All dots emerge from center
        previousCount <= 0 -> {
            target.map { targetPos -> lerp(center, targetPos) }
        }

        // 1→2: One dot in center duplicates and two dots repel horizontally
        previousCount == 1 && targetCount == 2 -> {
            listOf(
                Offset(x = center.x + (target[0].x - center.x) * progress, y = center.y),
                Offset(x = center.x + (target[1].x - center.x) * progress, y = center.y)
            )
        }

        // 1→3: Center dot splits into triangle positions
        previousCount == 1 && targetCount == 3 -> {
            target.map { targetPos -> lerp(center, targetPos) }
        }

        // 1→4: Center dot splits into 4 corners
        previousCount == 1 && targetCount == 4 -> {
            target.map { targetPos -> lerp(center, targetPos) }
        }

        // 1→5: Center dot splits into 4 corners + center stays
        previousCount == 1 && targetCount == 5 -> {
            target.map { targetPos -> lerp(center, targetPos) }
        }

        // 2→3: Third dot appears from center of two dots and moves up,
        // while the two dots slide down to form triangle
        previousCount == 2 && targetCount == 3 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(
                lerp(prev[0], target[0]),
                lerp(prev[1], target[1]),
                lerp(center, target[2])
            )
        }

        // 2→4: Left dot splits to top-left & bottom-left,
        //       right dot splits to top-right & bottom-right
        previousCount == 2 && targetCount == 4 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(
                lerp(prev[0], target[0]),  // left → top-left
                lerp(prev[1], target[1]),  // right → top-right
                lerp(prev[0], target[2]),  // left → bottom-left
                lerp(prev[1], target[3])   // right → bottom-right
            )
        }

        // 2→5: Left/right each split to two corners + center emerges
        previousCount == 2 && targetCount == 5 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(
                lerp(prev[0], target[0]),  // left → top-left
                lerp(prev[1], target[1]),  // right → top-right
                lerp(prev[0], target[2]),  // left → bottom-left
                lerp(prev[1], target[3]),  // right → bottom-right
                lerp(center, target[4])    // center emerges
            )
        }

        // 2→6: Left dot splits into left column (top/mid/bottom),
        //       right dot splits into right column (top/mid/bottom)
        previousCount == 2 && targetCount == 6 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(
                lerp(prev[0], target[0]),  // left → left-top
                lerp(prev[0], target[1]),  // left → left-mid
                lerp(prev[0], target[2]),  // left → left-bottom
                lerp(prev[1], target[3]),  // right → right-top
                lerp(prev[1], target[4]),  // right → right-mid
                lerp(prev[1], target[5])   // right → right-bottom
            )
        }

        // 3→4: Triangle bottom-left → square bottom-left,
        //       triangle bottom-right → square bottom-right,
        //       triangle top splits into square top-left + top-right
        previousCount == 3 && targetCount == 4 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(
                lerp(prev[2], target[0]),  // top → top-left
                lerp(prev[2], target[1]),  // top → top-right
                lerp(prev[0], target[2]),  // bottom-left → bottom-left
                lerp(prev[1], target[3])   // bottom-right → bottom-right
            )
        }

        // 3→5: Triangle bottom stays as bottom corners, top splits to top corners, center emerges
        previousCount == 3 && targetCount == 5 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(
                lerp(prev[2], target[0]),  // top → top-left
                lerp(prev[2], target[1]),  // top → top-right
                lerp(prev[0], target[2]),  // bottom-left → bottom-left
                lerp(prev[1], target[3]),  // bottom-right → bottom-right
                lerp(center, target[4])    // center emerges
            )
        }

        // 3→6: Triangle maps to die-6 (3 left, 3 right)
        //       bottom-left → left-bottom, bottom-right → right-bottom,
        //       top splits into left-top + right-top,
        //       middle dots emerge from center
        previousCount == 3 && targetCount == 6 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(
                lerp(prev[2], target[0]),   // top → left-top
                lerp(center, target[1]),    // center → left-mid
                lerp(prev[0], target[2]),   // bottom-left → left-bottom
                lerp(prev[2], target[3]),   // top → right-top
                lerp(center, target[4]),    // center → right-mid
                lerp(prev[1], target[5])    // bottom-right → right-bottom
            )
        }

        // 3→7: Triangle maps to hexagon + center
        //       top → hex-top, bottom-right → hex-bottom-right, bottom-left → hex-bottom-left,
        //       remaining hex vertices and center emerge from center
        previousCount == 3 && targetCount == 7 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(
                lerp(prev[2], target[0]),   // top → hex-top
                lerp(center, target[1]),    // center → hex-top-right
                lerp(prev[1], target[2]),   // bottom-right → hex-bottom-right
                lerp(center, target[3]),    // center → hex-bottom
                lerp(prev[0], target[4]),   // bottom-left → hex-bottom-left
                lerp(center, target[5]),    // center → hex-top-left
                lerp(center, target[6])     // center → center
            )
        }

        // Default: generic lerp from previous positions to target
        // Existing dots move to their new positions, extra dots emerge from center
        else -> {
            val prev = getDotPositions(previousCount, center, spread)
            target.mapIndexed { i, targetPos ->
                val from = if (i < prev.size) prev[i] else center
                lerp(from, targetPos)
            }
        }
    }
}

internal fun getDotPositions(count: Int, center: Offset, spread: Float): List<Offset> {
    return when (count) {
        1 -> listOf(center)
        2 -> listOf(
            Offset(center.x - spread * 0.9f, center.y),
            Offset(center.x + spread * 0.9f, center.y)
        )
        3 -> listOf(
            Offset(center.x - spread * 0.85f, center.y + spread * 0.55f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.55f),
            Offset(center.x, center.y - spread * 0.8f)
        )
        4 -> listOf(
            Offset(center.x - spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x - spread * 0.85f, center.y + spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.85f)
        )
        5 -> listOf(  // four corners + center (die face 5)
            Offset(center.x - spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x - spread * 0.85f, center.y + spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.85f),
            center
        )
        6 -> listOf(  // die face 6 (3 left column, 3 right column)
            Offset(center.x - spread * 0.85f, center.y - spread * 1.125f),
            Offset(center.x - spread * 0.85f, center.y),
            Offset(center.x - spread * 0.85f, center.y + spread * 1.125f),
            Offset(center.x + spread * 0.85f, center.y - spread * 1.125f),
            Offset(center.x + spread * 0.85f, center.y),
            Offset(center.x + spread * 0.85f, center.y + spread * 1.125f)
        )
        else -> listOf(  // 7+ dots: hexagon shape (6 vertices) + center dot
            Offset(center.x, center.y - spread * 1.125f),                             // top
            Offset(center.x + spread * 0.974f, center.y - spread * 0.5625f),          // top-right
            Offset(center.x + spread * 0.974f, center.y + spread * 0.5625f),          // bottom-right
            Offset(center.x, center.y + spread * 1.125f),                             // bottom
            Offset(center.x - spread * 0.974f, center.y + spread * 0.5625f),          // bottom-left
            Offset(center.x - spread * 0.974f, center.y - spread * 0.5625f),          // top-left
            center                                                                    // center
        )
    }
}
