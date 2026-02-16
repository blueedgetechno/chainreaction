package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.ui.components.GameGrid
import com.blueedge.chainreaction.ui.game.GameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColors
import kotlinx.coroutines.delay

@Composable
fun GameBoardScreen(
    onGameEnd: (winnerId: Int, p1Score: Int, p2Score: Int, moves: Int, duration: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Watch for game end
    LaunchedEffect(state.gameStatus) {
        if (state.gameStatus != GameStatus.IN_PROGRESS) {
            delay(2000)
            val winnerId = if (state.gameStatus == GameStatus.PLAYER1_WINS) 1 else 2
            onGameEnd(
                winnerId,
                state.player1Score,
                state.player2Score,
                state.moveCount,
                viewModel.getGameDurationSeconds()
            )
        }
    }

    val player1Color = PlayerColors.getOrElse(state.player1.colorIndex) { PlayerColors[0] }
    val player2Color = PlayerColors.getOrElse(state.player2.colorIndex) { PlayerColors[1] }

    // Vivid background that transitions between player colors
    val bgColor by animateColorAsState(
        targetValue = if (state.currentPlayerId == 1)
            Color(0xFF5BAAEF) // Vivid blue
        else
            Color(0xFFE99C7C), // Warm coral
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Grid centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.board.isNotEmpty()) {
                GameGrid(
                    board = state.board,
                    gridSize = state.gridSize,
                    currentPlayerId = state.currentPlayerId,
                    player1Color = player1Color,
                    player2Color = player2Color,
                    explodingCells = state.explodingCells,
                    onCellClick = { row, col -> viewModel.onCellClicked(row, col) },
                    isInteractionEnabled = !state.isAnimating &&
                            !state.botThinking &&
                            state.gameStatus == GameStatus.IN_PROGRESS,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }
        }

        // Exit capsule button — top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .clickable { showExitDialog = true }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "EXIT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        // Win announcement overlay
        AnimatedVisibility(
            visible = state.gameStatus != GameStatus.IN_PROGRESS,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val winnerName = if (state.gameStatus == GameStatus.PLAYER1_WINS)
                state.player1.name else state.player2.name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "\uD83C\uDF89", fontSize = 32.sp)
                Text(
                    text = "$winnerName Wins!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Game?") },
            text = { Text("Your progress will be lost. Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExit()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
