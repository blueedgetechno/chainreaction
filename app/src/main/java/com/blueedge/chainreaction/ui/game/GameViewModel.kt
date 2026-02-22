package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.createBot
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.CellState
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
        val playersList = config.getPlayers()
        return GameUiState(
            board = engine.createEmptyBoard(config.gridSize),
            gridSize = config.gridSize,
            currentPlayerId = 1,
            player1 = playersList.getOrElse(0) { PlayerInfo(1, "Player 1", 0) },
            player2 = playersList.getOrElse(1) { PlayerInfo(2, "Player 2", 1) },
            players = playersList,
            numPlayers = playersList.size,
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

        val isFirstMove = !currentState.playersHasMoved.contains(currentState.currentPlayerId)

        if (!engine.isValidMove(currentState.board, row, col, currentState.currentPlayerId, isFirstMove)) return

        viewModelScope.launch {
            executeMove(row, col)
        }
    }

    private suspend fun executeMove(row: Int, col: Int) {
        val currentState = _state.value
        val playerId = currentState.currentPlayerId
        val isFirstMove = !currentState.playersHasMoved.contains(playerId)

        _state.update { it.copy(isAnimating = true, lastMovedCell = Pair(row, col)) }

        // Play bop sound on cell tap
        SoundManager.playBop()

        // Execute the move in the game engine
        val (newBoard, explosionWaveData) = engine.executeMove(currentState.board, row, col, playerId, isFirstMove)

        // Show the intermediate board state (dots added, before explosion) for dot transition animation
        val intermediateBoard = currentState.board.map { it.toMutableList() }.toMutableList()
        if (isFirstMove) {
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = 3, previousDots = 0)
        } else {
            val currentCell = intermediateBoard[row][col]
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = currentCell.dots + 1, previousDots = currentCell.dots)
        }
        _state.update { it.copy(board = intermediateBoard.map { r -> r.toList() }) }

        // Wait for dot transition animation (250ms)
        delay(250)

        // Animate explosions wave by wave (BFS)
        for (waveData in explosionWaveData) {
            // Pause before each split to show the 4-dot state
            delay(200)

            // Play pop sound and vibrate for each explosion wave
            SoundManager.playPop()
            SoundManager.vibrate()

            // Phase 1: Show board with exploding cells emptied (cells disappear)
            _state.update {
                it.copy(
                    board = waveData.boardBeforeSplit,
                    explodingCells = waveData.explodingCells.map { m -> Pair(m.row, m.col) }.toSet(),
                    explosionMoves = waveData.moves
                )
            }
            delay(300) // Movement animation duration

            // Phase 2: Show board after dots arrived at neighbors
            _state.update {
                it.copy(
                    board = waveData.boardAfterSplit,
                    explodingCells = emptySet(),
                    explosionMoves = emptyList()
                )
            }
            // Allow dot transition animation to play (for newly formed 4-dot cells)
            delay(250)
        }

        // Clear explosion markers
        _state.update { it.copy(explodingCells = emptySet(), explosionMoves = emptyList()) }

        val newMoveCount = currentState.moveCount + 1
        val capturedCells = newBoard.sumOf { row -> row.count { it.dots > 0 } }
        val newPlayersHasMoved = currentState.playersHasMoved + playerId

        // Check win condition only after all players have made at least one move
        val winner = if (newPlayersHasMoved.size >= currentState.numPlayers) {
            engine.checkWinCondition(newBoard, newMoveCount)
        } else {
            null
        }
        val gameStatus = if (winner != null) GameStatus.GAME_OVER else GameStatus.IN_PROGRESS

        // Determine next player: cycle through active players (skip eliminated ones)
        val nextPlayer = getNextActivePlayer(newBoard, playerId, currentState.players, newPlayersHasMoved)

        _state.update { state ->
            state.copy(
                board = newBoard,
                currentPlayerId = if (gameStatus == GameStatus.IN_PROGRESS) nextPlayer else playerId,
                moveCount = newMoveCount,
                playersHasMoved = newPlayersHasMoved,
                capturedCells = capturedCells,
                gameStatus = gameStatus,
                winnerId = winner ?: 0,
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

    /**
     * Returns the ID of the next active player after [currentPlayerId].
     * Active = has cells on board OR hasn't made their first move yet.
     */
    private fun getNextActivePlayer(
        board: List<List<CellState>>,
        currentPlayerId: Int,
        players: List<PlayerInfo>,
        playersHasMoved: Set<Int>
    ): Int {
        val numPlayers = players.size
        // Try each candidate in turn order
        for (offset in 1..numPlayers) {
            val candidate = ((currentPlayerId - 1 + offset) % numPlayers) + 1
            val hasMoved = playersHasMoved.contains(candidate)
            val hasCells = board.any { row -> row.any { it.ownerId == candidate } }
            // Player is active if they haven't moved yet (first move) or still have cells
            if (!hasMoved || hasCells) return candidate
        }
        // Fallback: return next in rotation (shouldn't happen if game is still in progress)
        return (currentPlayerId % numPlayers) + 1
    }

    private suspend fun executeBotMove() {
        _state.update { it.copy(botThinking = true) }

        try {
            val currentState = _state.value
            val isBotFirstMove = !currentState.playersHasMoved.contains(2)
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
