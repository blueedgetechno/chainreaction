package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    val scale by animateFloatAsState(
        targetValue = if (isExploding) 1.5f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
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
            Offset(center.x - spread * 0.9f, center.y),
            Offset(center.x + spread * 0.9f, center.y)
        )
        3 -> listOf(
            Offset(center.x - spread * 0.85f, center.y + spread * 0.55f),
            Offset(center.x + spread * 0.85f, center.y + spread * 0.55f),
            Offset(center.x, center.y - spread * 0.8f)
        )
        else -> listOf(
            Offset(center.x - spread * 0.5f, center.y - spread * 0.5f),
            Offset(center.x + spread * 0.5f, center.y - spread * 0.5f),
            Offset(center.x - spread * 0.5f, center.y + spread * 0.5f),
            Offset(center.x + spread * 0.5f, center.y + spread * 0.5f)
        )
    }
}
