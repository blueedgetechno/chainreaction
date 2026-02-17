package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.ui.components.GameGrid
import com.blueedge.chainreaction.ui.game.GameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColors

@Composable
fun GameBoardScreen(
    onGameEnd: (winnerId: Int, p1Score: Int, p2Score: Int, moves: Int, duration: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val player1Color = PlayerColors.getOrElse(state.player1.colorIndex) { PlayerColors[0] }
    val player2Color = PlayerColors.getOrElse(state.player2.colorIndex) { PlayerColors[1] }

    // Vivid background that transitions between player colors
    val bgColor by animateColorAsState(
        targetValue = if (state.currentPlayerId == 1)
            Color(0xFF41AFD4) // Blue
        else
            Color(0xFFE99C7C), // Warm coral
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }

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
                    explosionMoves = state.explosionMoves,
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

        // Settings icon — top right, below status bar
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 12.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { showSettingsDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF333333),
                modifier = Modifier.size(22.dp)
            )
        }
    }

    // Victory popup dialog
    if (state.gameStatus != GameStatus.IN_PROGRESS) {
        val winnerName = if (state.gameStatus == GameStatus.PLAYER1_WINS)
            state.player1.name else state.player2.name
        val winnerColor = if (state.gameStatus == GameStatus.PLAYER1_WINS)
            player1Color else player2Color

        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "\uD83C\uDF89", fontSize = 64.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$winnerName\nWins!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = winnerColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val winnerId = if (state.gameStatus == GameStatus.PLAYER1_WINS) 1 else 2
                            onGameEnd(
                                winnerId,
                                state.player1Score,
                                state.player2Score,
                                state.moveCount,
                                viewModel.getGameDurationSeconds()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = winnerColor
                        )
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Settings dialog
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            showHowToPlay = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("How to Play")
                    }

                    Button(
                        onClick = {
                            showSettingsDialog = false
                            onExit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA695E)
                        )
                    ) {
                        Text("Exit to Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // How to Play dialog
    if (showHowToPlay) {
        Dialog(onDismissRequest = { showHowToPlay = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How to Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap on empty cells to place your dots. " +
                                "When a cell reaches 4 dots, it explodes and sends dots to adjacent cells. " +
                                "Capture all opponent cells to win!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showHowToPlay = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}
