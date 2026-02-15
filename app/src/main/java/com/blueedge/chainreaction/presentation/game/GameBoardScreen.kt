package com.blueedge.chainreaction.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blueedge.chainreaction.data.model.GameMode
import com.blueedge.chainreaction.data.model.GameState
import com.blueedge.chainreaction.presentation.game.components.GameGrid
import com.blueedge.chainreaction.presentation.game.components.ScoreBar
import com.blueedge.chainreaction.presentation.game.components.TurnIndicator

/**
 * Game board screen - main gameplay screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoardScreen(
    gridSize: Int,
    gameMode: GameMode,
    onNavigateToEnd: (winnerId: Int, moves: Int, duration: Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val boardState by viewModel.boardState.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val players by viewModel.players.collectAsState()
    
    // Initialize game when screen is first composed
    LaunchedEffect(gridSize, gameMode) {
        viewModel.initializeGame(gridSize, gameMode)
    }
    
    // Handle game over
    LaunchedEffect(gameState) {
        if (gameState is GameState.GameOver) {
            val gameOver = gameState as GameState.GameOver
            onNavigateToEnd(
                gameOver.winnerId,
                gameOver.totalMoves,
                gameOver.gameDuration
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chain Reaction",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Bar
            if (players.size >= 2) {
                ScoreBar(
                    players = players,
                    player1Cells = boardState.getCellCount(players[0].id),
                    player2Cells = boardState.getCellCount(players[1].id)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Game Grid
            GameGrid(
                boardState = boardState,
                players = players,
                currentPlayerId = currentPlayer?.id ?: 1,
                onCellClick = { row, col ->
                    viewModel.onCellClicked(row, col)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Turn Indicator
            currentPlayer?.let { player ->
                TurnIndicator(currentPlayer = player)
            }
        }
    }
}
