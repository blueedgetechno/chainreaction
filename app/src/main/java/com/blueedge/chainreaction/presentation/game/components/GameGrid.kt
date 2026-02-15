package com.blueedge.chainreaction.presentation.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blueedge.chainreaction.data.model.BoardState
import com.blueedge.chainreaction.data.model.Player

/**
 * Main game grid component
 */
@Composable
fun GameGrid(
    boardState: BoardState,
    players: List<Player>,
    currentPlayerId: Int,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = boardState.gridSize
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp),
        userScrollEnabled = false
    ) {
        items(gridSize * gridSize) { index ->
            val row = index / gridSize
            val col = index % gridSize
            val cell = boardState.getCell(row, col)
            
            val playerColor = cell?.playerId?.let { playerId ->
                players.find { it.id == playerId }?.color
            }
            
            val isClickable = cell?.let {
                it.isEmpty() || it.playerId == currentPlayerId
            } ?: false
            
            GridCell(
                dots = cell?.dots ?: 0,
                playerColor = playerColor,
                isClickable = isClickable,
                onClick = { onCellClick(row, col) }
            )
        }
    }
}
