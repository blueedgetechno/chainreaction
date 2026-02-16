package com.blueedge.chainreaction.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.ripple
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.ui.theme.CellEmptyDark
import com.blueedge.chainreaction.ui.theme.CellEmptyLight

@Composable
fun GameGrid(
    board: List<List<CellState>>,
    gridSize: Int,
    currentPlayerId: Int,
    player1Color: Color,
    player2Color: Color,
    explodingCells: Set<Pair<Int, Int>>,
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
                        val ownerColor = when (cell.ownerId) {
                            1 -> player1Color
                            2 -> player2Color
                            else -> Color.Transparent
                        }

                        val currentColor = if (currentPlayerId == 1) player1Color else player2Color

                        GridCell(
                            cellState = cell,
                            ownerColor = ownerColor,
                            currentPlayerColor = currentColor,
                            isExploding = explodingCells.contains(Pair(row, col)),
                            isCurrentPlayer = cell.ownerId == currentPlayerId,
                            onClick = {
                                if (isInteractionEnabled) onCellClick(row, col)
                            },
                            modifier = Modifier.size(cellSize)
                        )
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
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val emptyColor = if (isDark) CellEmptyDark else CellEmptyLight

    val cellBackground by animateColorAsState(
        targetValue = emptyColor,
        animationSpec = tween(400),
        label = "cellColor"
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .background(cellBackground, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = currentPlayerColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!cellState.isEmpty) {
            DotCircle(
                dotCount = cellState.dots,
                color = ownerColor,
                isCurrentPlayer = isCurrentPlayer,
                isExploding = isExploding
            )
        }
    }
}
