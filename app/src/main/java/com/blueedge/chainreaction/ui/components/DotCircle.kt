package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun DotCircle(
    dotCount: Int,
    color: Color,
    isCurrentPlayer: Boolean,
    isExploding: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExploding) 1.5f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha = if (isCurrentPlayer && !isExploding) pulseAlpha else 1f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
    ) {
        val circleRadius = size.minDimension * 0.38f
        val center = Offset(size.width / 2, size.height / 2)

        // Draw main circle
        drawCircle(
            color = color,
            radius = circleRadius,
            center = center
        )

        // Draw white dots
        val dotRadius = circleRadius * 0.2f
        val dotPositions = getDotPositions(dotCount, center, circleRadius * 0.45f)

        for (pos in dotPositions) {
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = pos
            )
        }
    }
}

private fun getDotPositions(count: Int, center: Offset, spread: Float): List<Offset> {
    return when (count) {
        1 -> listOf(center)
        2 -> listOf(
            Offset(center.x - spread * 0.5f, center.y - spread * 0.5f),
            Offset(center.x + spread * 0.5f, center.y + spread * 0.5f)
        )
        3 -> listOf(
            Offset(center.x - spread * 0.6f, center.y - spread * 0.6f),
            center,
            Offset(center.x + spread * 0.6f, center.y + spread * 0.6f)
        )
        else -> listOf(
            Offset(center.x - spread * 0.5f, center.y - spread * 0.5f),
            Offset(center.x + spread * 0.5f, center.y - spread * 0.5f),
            Offset(center.x - spread * 0.5f, center.y + spread * 0.5f),
            Offset(center.x + spread * 0.5f, center.y + spread * 0.5f)
        )
    }
}
