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
    val needsAnimation = previousDots != dotCount
    val animProgress = remember(dotCount, previousDots) {
        Animatable(if (needsAnimation) 0f else 1f)
    }

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
        modifier = modifier.fillMaxSize()
    ) {
        val circleRadius = size.minDimension * 0.38f
        val center = Offset(size.width / 2, size.height / 2)

        drawCircle(color = color, radius = circleRadius, center = center)

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

private fun getAnimatedDotPositions(
    previousCount: Int,
    targetCount: Int,
    center: Offset,
    spread: Float,
    progress: Float
): List<Offset> {
    val target = getDotPositions(targetCount, center, spread)

    if (progress >= 1f || previousCount == targetCount) return target

    fun lerp(from: Offset, to: Offset): Offset = Offset(
        x = from.x + (to.x - from.x) * progress,
        y = from.y + (to.y - from.y) * progress
    )

    return when {
        previousCount <= 0 -> target.map { lerp(center, it) }
        previousCount == 1 && targetCount == 2 -> listOf(
            Offset(x = center.x + (target[0].x - center.x) * progress, y = center.y),
            Offset(x = center.x + (target[1].x - center.x) * progress, y = center.y)
        )
        previousCount == 1 && targetCount == 3 -> target.map { lerp(center, it) }
        previousCount == 1 && targetCount == 4 -> target.map { lerp(center, it) }
        previousCount == 1 && targetCount == 5 -> target.map { lerp(center, it) }
        previousCount == 2 && targetCount == 3 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(lerp(prev[0], target[0]), lerp(prev[1], target[1]), lerp(center, target[2]))
        }
        previousCount == 2 && targetCount == 4 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(lerp(prev[0], target[0]), lerp(prev[1], target[1]), lerp(prev[0], target[2]), lerp(prev[1], target[3]))
        }
        previousCount == 2 && targetCount == 5 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(lerp(prev[0], target[0]), lerp(prev[1], target[1]), lerp(prev[0], target[2]), lerp(prev[1], target[3]), lerp(center, target[4]))
        }
        previousCount == 2 && targetCount == 6 -> {
            val prev = getDotPositions(2, center, spread)
            listOf(lerp(prev[0], target[0]), lerp(prev[0], target[1]), lerp(prev[0], target[2]), lerp(prev[1], target[3]), lerp(prev[1], target[4]), lerp(prev[1], target[5]))
        }
        previousCount == 3 && targetCount == 4 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(lerp(prev[2], target[0]), lerp(prev[2], target[1]), lerp(prev[0], target[2]), lerp(prev[1], target[3]))
        }
        previousCount == 3 && targetCount == 5 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(lerp(prev[2], target[0]), lerp(prev[2], target[1]), lerp(prev[0], target[2]), lerp(prev[1], target[3]), lerp(center, target[4]))
        }
        previousCount == 3 && targetCount == 6 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(lerp(prev[2], target[0]), lerp(center, target[1]), lerp(prev[0], target[2]), lerp(prev[2], target[3]), lerp(center, target[4]), lerp(prev[1], target[5]))
        }
        previousCount == 3 && targetCount == 7 -> {
            val prev = getDotPositions(3, center, spread)
            listOf(lerp(prev[2], target[0]), lerp(center, target[1]), lerp(prev[1], target[2]), lerp(center, target[3]), lerp(prev[0], target[4]), lerp(center, target[5]), lerp(center, target[6]))
        }
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
        5 -> listOf(
            Offset(center.x - spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y - spread * 0.85f),
            Offset(center.x - spread * 0.85f, center.y + spread * 0.85f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.85f),
            center
        )
        6 -> listOf(
            Offset(center.x - spread * 0.85f, center.y - spread * 1.125f),
            Offset(center.x - spread * 0.85f, center.y),
            Offset(center.x - spread * 0.85f, center.y + spread * 1.125f),
            Offset(center.x + spread * 0.85f, center.y - spread * 1.125f),
            Offset(center.x + spread * 0.85f, center.y),
            Offset(center.x + spread * 0.85f, center.y + spread * 1.125f)
        )
        else -> listOf(
            Offset(center.x, center.y - spread * 1.125f),
            Offset(center.x + spread * 0.974f, center.y - spread * 0.5625f),
            Offset(center.x + spread * 0.974f, center.y + spread * 0.5625f),
            Offset(center.x, center.y + spread * 1.125f),
            Offset(center.x - spread * 0.974f, center.y + spread * 0.5625f),
            Offset(center.x - spread * 0.974f, center.y - spread * 0.5625f),
            center
        )
    }
}
