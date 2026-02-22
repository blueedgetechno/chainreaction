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
    val needsAnimation = previousDots != dotCount && previousDots >= 0
    val animProgress = remember(dotCount, previousDots) {
        Animatable(if (needsAnimation) 0f else 1f)
    }

    // Separate fade-in alpha for 0→1 transition (dot appearing on empty cell)
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
 * - 0→1 or 1→1: Single dot at center
 * - 1→2: One dot splits from center into two horizontal positions
 * - 2→3: Third dot emerges from center of two, moves up; existing two slide down to form triangle
 * - 3→4: Fourth dot copies from top position, 3rd and 4th repel horizontally to form square top edge
 * - 4→5: Fifth dot appears at center between the four square-corner dots
 * - 0→3 (first move): Dots appear from center and spread to triangle positions
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

    return when {
        // 0→1: Dot fades in at center (no position animation needed)
        previousCount == 0 && targetCount == 1 -> target

        // 0→3 (first move): All dots emerge from center
        previousCount == 0 && targetCount == 3 -> {
            target.map { targetPos ->
                Offset(
                    x = center.x + (targetPos.x - center.x) * progress,
                    y = center.y + (targetPos.y - center.y) * progress
                )
            }
        }

        // 1→2: One dot in center duplicates and two dots repel horizontally
        previousCount == 1 && targetCount == 2 -> {
            val leftTarget = target[0]
            val rightTarget = target[1]
            listOf(
                Offset(
                    x = center.x + (leftTarget.x - center.x) * progress,
                    y = center.y
                ),
                Offset(
                    x = center.x + (rightTarget.x - center.x) * progress,
                    y = center.y
                )
            )
        }

        // 2→3: Third dot appears from center of two dots and moves up,
        // while the two dots slide down to form triangle
        previousCount == 2 && targetCount == 3 -> {
            val prevPositions = getDotPositions(2, center, spread)
            // Two existing dots slide from horizontal to triangle bottom
            val dot1 = Offset(
                x = prevPositions[0].x + (target[0].x - prevPositions[0].x) * progress,
                y = prevPositions[0].y + (target[0].y - prevPositions[0].y) * progress
            )
            val dot2 = Offset(
                x = prevPositions[1].x + (target[1].x - prevPositions[1].x) * progress,
                y = prevPositions[1].y + (target[1].y - prevPositions[1].y) * progress
            )
            // Third dot emerges from center and moves up
            val dot3 = Offset(
                x = center.x + (target[2].x - center.x) * progress,
                y = center.y + (target[2].y - center.y) * progress
            )
            listOf(dot1, dot2, dot3)
        }

        // 3→4: Fourth dot copies from top of triangle (position of dot 3),
        // then 3rd and 4th repel horizontally to form top edge of square
        // Mapping: triangle bottom-left → square bottom-left (target[2]),
        //          triangle bottom-right → square bottom-right (target[3]),
        //          triangle top → splits into square top-left (target[0]) + top-right (target[1])
        previousCount == 3 && targetCount == 4 -> {
            val prevPositions = getDotPositions(3, center, spread)
            // Bottom two dots slide from triangle bottom to square bottom
            val dot1 = Offset(
                x = prevPositions[0].x + (target[2].x - prevPositions[0].x) * progress,
                y = prevPositions[0].y + (target[2].y - prevPositions[0].y) * progress
            )
            val dot2 = Offset(
                x = prevPositions[1].x + (target[3].x - prevPositions[1].x) * progress,
                y = prevPositions[1].y + (target[3].y - prevPositions[1].y) * progress
            )
            // Top dot of triangle (prevPositions[2]) splits into two top dots of square
            val topCenter = prevPositions[2]
            val dot3 = Offset(
                x = topCenter.x + (target[0].x - topCenter.x) * progress,
                y = topCenter.y + (target[0].y - topCenter.y) * progress
            )
            val dot4 = Offset(
                x = topCenter.x + (target[1].x - topCenter.x) * progress,
                y = topCenter.y + (target[1].y - topCenter.y) * progress
            )
            listOf(dot1, dot2, dot3, dot4)
        }

        // 4→5: Fifth dot appears at center from between the four square corners
        previousCount == 4 && targetCount == 5 -> {
            val prevPositions = getDotPositions(4, center, spread)
            // Four corner dots stay in place
            val corners = prevPositions.mapIndexed { i, prev ->
                Offset(
                    x = prev.x + (target[i].x - prev.x) * progress,
                    y = prev.y + (target[i].y - prev.y) * progress
                )
            }
            // Fifth dot emerges from center
            val dot5 = Offset(
                x = center.x + (target[4].x - center.x) * progress,
                y = center.y + (target[4].y - center.y) * progress
            )
            corners + dot5
        }

        // Default: just show target positions
        else -> target
    }
}

private fun getDotPositions(count: Int, center: Offset, spread: Float): List<Offset> {
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
        else -> listOf(  // 5+ dots: four corners + center
            Offset(center.x - spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x - spread * 0.85f, center.y + spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.85f),
            center  // center dot
        )
    }
}
