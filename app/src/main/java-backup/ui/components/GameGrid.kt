package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.ExplosionMove
import com.blueedge.chainreaction.ui.theme.CellEmptyLight

@Composable
fun GameGrid(
    board: List<List<CellState>>,
    gridSize: Int,
    currentPlayerId: Int,
    playerColors: List<Color>,
    explodingCells: Set<Pair<Int, Int>>,
    explosionMoves: List<ExplosionMove> = emptyList(),
    onCellClick: (Int, Int) -> Unit,
    isInteractionEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val calculatedCellSize = min(maxWidth / gridSize, maxHeight / gridSize)
        val cellSize = calculatedCellSize
        val gridWidth = cellSize * gridSize
        val gridHeight = cellSize * gridSize
        val cellSizePx = with(LocalDensity.current) { cellSize.toPx() }
        // Border radius proportional to cell width
        val cellCornerRadius = cellSize * 0.25f

        Box(
            modifier = Modifier
                .width(gridWidth)
                .height(gridHeight)
        ) {
            Column(
                modifier = Modifier
                    .width(gridWidth)
                    .height(gridHeight),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until gridSize) {
                    Row {
                        for (col in 0 until gridSize) {
                            val cell = board[row][col]
                            val ownerColor = if (cell.ownerId > 0)
                                playerColors.getOrElse(cell.ownerId - 1) { Color.Gray }
                            else Color.Transparent

                            val currentColor = playerColors.getOrElse(currentPlayerId - 1) { playerColors[0] }

                            GridCell(
                                cellState = cell,
                                ownerColor = ownerColor,
                                currentPlayerColor = currentColor,
                                isExploding = explodingCells.contains(Pair(row, col)),
                                isCurrentPlayer = cell.ownerId == currentPlayerId,
                                onClick = {
                                    if (isInteractionEnabled) onCellClick(row, col)
                                },
                                cellCornerRadius = cellCornerRadius,
                                modifier = Modifier.size(cellSize)
                            )
                        }
                    }
                }
            }

            // Explosion movement overlay: animated circles moving from source to target cells
            if (explosionMoves.isNotEmpty()) {
                val moveProgress = remember { Animatable(0f) }

                LaunchedEffect(explosionMoves) {
                    moveProgress.snapTo(0f)
                    moveProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }

                Canvas(
                    modifier = Modifier
                        .width(gridWidth)
                        .height(gridHeight)
                ) {
                    val progress = moveProgress.value
                    val circleRadius = cellSizePx * 0.38f
                    val dotRadius = circleRadius * 0.2f
                    val spread = circleRadius * 0.45f

                    // Pass 1: Draw all travelling circles (simple translate)
                    for (move in explosionMoves) {
                        val fromCenter = Offset(
                            x = (move.fromCol + 0.5f) * cellSizePx,
                            y = (move.fromRow + 0.5f) * cellSizePx
                        )
                        val toCenter = Offset(
                            x = (move.toCol + 0.5f) * cellSizePx,
                            y = (move.toRow + 0.5f) * cellSizePx
                        )
                        val currentPos = Offset(
                            x = fromCenter.x + (toCenter.x - fromCenter.x) * progress,
                            y = fromCenter.y + (toCenter.y - fromCenter.y) * progress
                        )

                        val moveColor = playerColors.getOrElse(move.playerId - 1) { Color.Gray }

                        drawCircle(
                            color = moveColor,
                            radius = circleRadius,
                            center = currentPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = dotRadius,
                            center = currentPos
                        )
                    }

                    // Pass 2: Re-draw each target cell's existing white dots on top of any
                    // travelling circle, so the original dot count is never obscured.
                    for (move in explosionMoves) {
                        val targetCell = board[move.toRow][move.toCol]
                        if (!targetCell.isEmpty) {
                            val targetCenter = Offset(
                                x = (move.toCol + 0.5f) * cellSizePx,
                                y = (move.toRow + 0.5f) * cellSizePx
                            )
                            val dotPositions = getDotPositions(targetCell.dots, targetCenter, spread)
                            for (pos in dotPositions) {
                                drawCircle(
                                    color = Color.White,
                                    radius = dotRadius,
                                    center = pos
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    cellState: CellState,
    ownerColor: Color,
    currentPlayerColor: Color,
    isExploding: Boolean,
    isCurrentPlayer: Boolean,
    onClick: () -> Unit,
    cellCornerRadius: androidx.compose.ui.unit.Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    // Always use light cell color (forced light mode)
    val baseColor = CellEmptyLight
    // If cell is owned by the current player, use a brighter, less saturated version of their color
    val targetColor = if (!cellState.isEmpty && isCurrentPlayer) {
        val hsl = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.argb(
                (currentPlayerColor.alpha * 255).toInt(),
                (currentPlayerColor.red * 255).toInt(),
                (currentPlayerColor.green * 255).toInt(),
                (currentPlayerColor.blue * 255).toInt()
            ),
            hsl
        )
        hsl[1] = (hsl[1] * 0.42f).coerceIn(0f, 1f)  // reduce saturation
        hsl[2] = (hsl[2] + (1f - hsl[2]) * 0.75f).coerceIn(0f, 1f)  // push brightness up
        Color(android.graphics.Color.HSVToColor(hsl))
    } else {
        baseColor
    }

    val cellBackground by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(400),
        label = "cellColor"
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .background(cellBackground, RoundedCornerShape(cellCornerRadius))
            .clip(RoundedCornerShape(cellCornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!cellState.isEmpty) {
            DotCircle(
                dotCount = cellState.dots,
                color = ownerColor,
                isCurrentPlayer = isCurrentPlayer,
                isExploding = isExploding,
                previousDots = cellState.previousDots
            )
        }
    }
}
