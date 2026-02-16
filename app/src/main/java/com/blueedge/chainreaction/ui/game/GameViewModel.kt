package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.createBot
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameUiState
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.domain.GameEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val engine = GameEngine()
    private var botStrategy: BotStrategy? = null

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        if (GameConfig.gameMode == GameMode.VS_BOT) {
            botStrategy = createBot(GameConfig.botDifficulty)
        }
    }

    private fun createInitialState(): GameUiState {
        val config = GameConfig
        return GameUiState(
            board = engine.createEmptyBoard(config.gridSize),
            gridSize = config.gridSize,
            currentPlayerId = 1,
            player1 = PlayerInfo(
                id = 1,
                name = config.player1Name,
                colorIndex = config.player1ColorIndex
            ),
            player2 = PlayerInfo(
                id = 2,
                name = if (config.gameMode == GameMode.VS_BOT) config.player2Name else config.player2Name,
                colorIndex = config.player2ColorIndex,
                isBot = config.gameMode == GameMode.VS_BOT
            ),
            gameMode = config.gameMode,
            botDifficulty = config.botDifficulty,
            gameStartTimeMs = System.currentTimeMillis()
        )
    }

    fun onCellClicked(row: Int, col: Int) {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.IN_PROGRESS) return
        if (currentState.isAnimating) return
        if (currentState.botThinking) return

        val isFirstMove = if (currentState.currentPlayerId == 1) !currentState.player1HasMoved else !currentState.player2HasMoved

        if (!engine.isValidMove(currentState.board, row, col, currentState.currentPlayerId, isFirstMove)) return

        viewModelScope.launch {
            executeMove(row, col)
        }
    }

    private suspend fun executeMove(row: Int, col: Int) {
        val currentState = _state.value
        val playerId = currentState.currentPlayerId
        val isFirstMove = if (playerId == 1) !currentState.player1HasMoved else !currentState.player2HasMoved

        _state.update { it.copy(isAnimating = true, lastMovedCell = Pair(row, col)) }

        // Execute the move in the game engine
        val (newBoard, explosionWaves) = engine.executeMove(currentState.board, row, col, playerId, isFirstMove)

        // Animate explosions wave by wave
        for (wave in explosionWaves) {
            _state.update {
                it.copy(explodingCells = wave.map { m -> Pair(m.row, m.col) }.toSet())
            }
            delay(350) // Animation delay per wave
        }

        // Clear explosion markers
        _state.update { it.copy(explodingCells = emptySet()) }

        val newMoveCount = currentState.moveCount + 1
        val p1Score = engine.countPlayerCells(newBoard, 1)
        val p2Score = engine.countPlayerCells(newBoard, 2)

        // Check win condition
        val winner = engine.checkWinCondition(newBoard, newMoveCount)
        val gameStatus = when (winner) {
            1 -> GameStatus.PLAYER1_WINS
            2 -> GameStatus.PLAYER2_WINS
            else -> GameStatus.IN_PROGRESS
        }

        val nextPlayer = if (playerId == 1) 2 else 1

        _state.update { state ->
            state.copy(
                board = newBoard,
                currentPlayerId = if (gameStatus == GameStatus.IN_PROGRESS) nextPlayer else playerId,
                moveCount = newMoveCount,
                player1HasMoved = if (playerId == 1) true else state.player1HasMoved,
                player2HasMoved = if (playerId == 2) true else state.player2HasMoved,
                player1Score = p1Score,
                player2Score = p2Score,
                gameStatus = gameStatus,
                isAnimating = false,
                lastMovedCell = null
            )
        }

        // If it's bot's turn and game is still in progress
        if (gameStatus == GameStatus.IN_PROGRESS &&
            _state.value.gameMode == GameMode.VS_BOT &&
            nextPlayer == 2
        ) {
            executeBotMove()
        }
    }

    private suspend fun executeBotMove() {
        _state.update { it.copy(botThinking = true) }

        try {
            val currentState = _state.value
            val isBotFirstMove = !currentState.player2HasMoved
            val move = botStrategy?.calculateMove(currentState.board, 2, 1, isBotFirstMove)

            _state.update { it.copy(botThinking = false) }

            if (move != null) {
                executeMove(move.row, move.col)
            }
        } catch (e: Exception) {
            _state.update { it.copy(botThinking = false) }
        }
    }

    fun getGameDurationSeconds(): Long {
        return (System.currentTimeMillis() - _state.value.gameStartTimeMs) / 1000
    }

    fun resetGame() {
        _state.value = createInitialState()
    }
}
