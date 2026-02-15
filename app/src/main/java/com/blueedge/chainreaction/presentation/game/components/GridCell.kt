package com.blueedge.chainreaction.presentation.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Individual cell in the game grid that displays dots
 */
@Composable
fun GridCell(
    dots: Int,
    playerColor: Color?,
    isClickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        if (dots > 0 && playerColor != null) {
            DotCircle(
                dotCount = dots,
                color = playerColor
            )
        }
    }
}

/**
 * Draws a circle with dots representing the cell state
 */
@Composable
fun DotCircle(
    dotCount: Int,
    color: Color
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val circleRadius = size.minDimension * 0.35f
        val dotRadius = size.minDimension * 0.08f
        
        // Draw background circle
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = circleRadius,
            center = Offset(centerX, centerY)
        )
        
        // Draw dots in die pattern
        val dotOffset = circleRadius * 0.4f
        
        when (dotCount) {
            1 -> {
                // Center dot
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX, centerY)
                )
            }
            2 -> {
                // Top-left and bottom-right
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX - dotOffset, centerY - dotOffset)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX + dotOffset, centerY + dotOffset)
                )
            }
            3 -> {
                // Center, top-left, bottom-right
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX - dotOffset, centerY - dotOffset)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX + dotOffset, centerY + dotOffset)
                )
            }
            4 -> {
                // All four corners
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX - dotOffset, centerY - dotOffset)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX + dotOffset, centerY - dotOffset)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX - dotOffset, centerY + dotOffset)
                )
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(centerX + dotOffset, centerY + dotOffset)
                )
            }
        }
    }
}
