package com.blueedge.chainreaction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.ui.components.GameGrid
import com.blueedge.chainreaction.ui.components.ScoreBar
import com.blueedge.chainreaction.ui.components.TurnIndicator
import com.blueedge.chainreaction.ui.game.GameViewModel
import com.blueedge.chainreaction.ui.theme.PlayerColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
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
            delay(2000) // Let player see final state
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
    val currentPlayerColor = if (state.currentPlayerId == 1) player1Color else player2Color

    var showExitDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            currentPlayerColor.copy(alpha = 0.08f),
            currentPlayerColor.copy(alpha = 0.15f),
            currentPlayerColor.copy(alpha = 0.08f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chain Reaction") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Game") },
                                onClick = {
                                    showMenu = false
                                    viewModel.resetGame()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Exit to Main Menu") },
                                onClick = {
                                    showMenu = false
                                    showExitDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bgGradient)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score bar
                ScoreBar(
                    player1 = state.player1,
                    player2 = state.player2,
                    player1Score = state.player1Score,
                    player2Score = state.player2Score,
                    player1Color = player1Color,
                    player2Color = player2Color,
                    currentPlayerId = state.currentPlayerId
                )

                // Game grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Turn indicator
                val currentPlayer =
                    if (state.currentPlayerId == 1) state.player1 else state.player2
                TurnIndicator(
                    playerName = currentPlayer.name,
                    playerColor = currentPlayerColor,
                    isBotThinking = state.botThinking
                )

                // Win announcement overlay
                AnimatedVisibility(
                    visible = state.gameStatus != GameStatus.IN_PROGRESS,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(300))
                ) {
                    val winnerName = if (state.gameStatus == GameStatus.PLAYER1_WINS)
                        state.player1.name else state.player2.name
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "$winnerName Wins!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = currentPlayerColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
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
