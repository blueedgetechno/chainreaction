package com.blueedge.chainreaction.presentation.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.data.model.*
import com.blueedge.chainreaction.data.repository.GameRepository
import com.blueedge.chainreaction.domain.GameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the game board managing game state and logic
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val gameRepository: GameRepository
) : ViewModel() {
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Setup)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _boardState = MutableStateFlow(BoardState.empty(6))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()
    
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()
    
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()
    
    private var gameMode: GameMode = GameMode.LOCAL_MULTIPLAYER
    private var moveCount = 0
    private var gameStartTime = 0L
    
    fun initializeGame(gridSize: Int, mode: GameMode) {
        gameMode = mode
        _boardState.value = BoardState.empty(gridSize)
        
        // Initialize players with default colors
        val player1 = Player(
            id = 1,
            name = "Player 1",
            color = Color(0xFF2196F3) // Blue
        )
        val player2 = Player(
            id = 2,
            name = if (mode.isBot) "Bot" else "Player 2",
            color = Color(0xFFF44336) // Red
        )
        
        _players.value = listOf(player1, player2)
        _currentPlayer.value = player1
        
        moveCount = 0
        gameStartTime = System.currentTimeMillis()
        
        _gameState.value = GameState.Playing(
            currentPlayerId = player1.id,
            moveCount = 0,
            gameStartTime = gameStartTime
        )
    }
    
    fun onCellClicked(row: Int, col: Int) {
        val state = _gameState.value
        if (state !is GameState.Playing) return
        
        val player = _currentPlayer.value ?: return
        
        // Check if move is valid
        if (!gameEngine.isValidMove(_boardState.value, row, col, player)) {
            return
        }
        
        // Execute the move
        viewModelScope.launch {
            executeMove(row, col, player)
        }
    }
    
    private suspend fun executeMove(row: Int, col: Int, player: Player) {
        // Add dot and get result with explosions
        val result = gameEngine.addDot(_boardState.value, row, col, player)
        
        // Update board state
        _boardState.value = result.newBoard
        moveCount++
        
        // Small delay to show the move
        delay(100)
        
        // Check win condition
        val winner = gameEngine.checkWinCondition(result.newBoard, _players.value)
        if (winner != null) {
            endGame(winner)
            return
        }
        
        // Switch to next player
        switchTurn()
        
        // If bot mode and it's bot's turn, execute bot move
        if (gameMode.isBot && _currentPlayer.value?.id == 2) {
            delay(1000) // Bot thinking time
            executeBotMove()
        }
    }
    
    private fun switchTurn() {
        val currentPlayerId = _currentPlayer.value?.id ?: 1
        val nextPlayerId = if (currentPlayerId == 1) 2 else 1
        _currentPlayer.value = _players.value.find { it.id == nextPlayerId }
        
        _gameState.value = GameState.Playing(
            currentPlayerId = nextPlayerId,
            moveCount = moveCount,
            gameStartTime = gameStartTime
        )
    }
    
    private suspend fun executeBotMove() {
        val player = _currentPlayer.value ?: return
        
        // Get valid moves
        val validMoves = gameEngine.getValidMoves(_boardState.value, player)
        if (validMoves.isEmpty()) return
        
        // For now, just pick a random move (Easy bot)
        // TODO: Implement Medium and Hard bots in Phase 7
        val move = validMoves.random()
        executeMove(move.row, move.col, player)
    }
    
    private fun endGame(winner: Player) {
        val duration = (System.currentTimeMillis() - gameStartTime) / 1000
        
        _gameState.value = GameState.GameOver(
            winnerId = winner.id,
            gameDuration = duration,
            totalMoves = moveCount
        )
        
        // Save game statistics
        viewModelScope.launch {
            val loser = _players.value.find { it.id != winner.id }
            
            gameRepository.saveGameStat(
                com.blueedge.chainreaction.data.local.entities.GameStatEntity(
                    gameMode = if (gameMode.isBot) "BOT" else "LOCAL",
                    winner = winner.name,
                    loser = loser?.name ?: "Unknown",
                    gridSize = _boardState.value.gridSize,
                    totalMoves = moveCount,
                    durationSeconds = duration,
                    difficulty = gameMode.difficulty?.name
                )
            )
        }
    }
}
