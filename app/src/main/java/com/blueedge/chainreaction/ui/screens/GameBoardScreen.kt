package com.blueedge.chainreaction.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onOpenSettings: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val player1Color = PlayerColors.getOrElse(state.player1.colorIndex) { PlayerColors[0] }
    val player2Color = PlayerColors.getOrElse(state.player2.colorIndex) { PlayerColors[1] }

    // Vivid background that transitions between player colors
    val bgColor by animateColorAsState(
        targetValue = when (state.currentPlayerId) {
            1 -> player1Color
            2 -> player2Color
            else -> Color(0xFF41AFD4)
        },
        animationSpec = tween(600),
        label = "bgColor"
    )

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    // Handle back button press
    BackHandler(enabled = state.gameStatus == GameStatus.IN_PROGRESS) {
        showExitConfirmation = true
    }

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
                .clickable { onOpenSettings() },
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

    // Full-screen victory overlay
    if (state.gameStatus != GameStatus.IN_PROGRESS) {
        val winnerName = if (state.gameStatus == GameStatus.PLAYER1_WINS)
            state.player1.name else state.player2.name
        val winnerColor = if (state.gameStatus == GameStatus.PLAYER1_WINS)
            player1Color else player2Color

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(winnerColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "\uD83C\uDFC6",
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "$winnerName",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "WINS!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Play Again button with shadow
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Shadow layer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .offset(y = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    // Main button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .clickable {
                                val winnerId = if (state.gameStatus == GameStatus.PLAYER1_WINS) 1 else 2
                                onGameEnd(
                                    winnerId,
                                    state.player1Score,
                                    state.player2Score,
                                    state.moveCount,
                                    viewModel.getGameDurationSeconds()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Play Again",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = winnerColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Menu button with shadow
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Shadow layer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .offset(y = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    // Main button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable { onExit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Menu",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Exit confirmation dialog
    if (showExitConfirmation) {
        Dialog(onDismissRequest = { showExitConfirmation = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Exit Game?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Are you sure you want to exit? Your game progress will be lost.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE0E0E0))
                                .clickable { showExitConfirmation = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        // Exit button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEA695E))
                                .clickable {
                                    showExitConfirmation = false
                                    onExit()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Exit",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
