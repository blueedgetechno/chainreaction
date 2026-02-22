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
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.domain.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameViewModel : ViewModel() {

    private val engine = GameEngine(GameConfig.gameVariant)
    private var botStrategy: BotStrategy? = null
    private var moveJob: Job? = null
    private val pauseMutex = Mutex()

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        if (GameConfig.gameMode == GameMode.VS_BOT) {
            botStrategy = createBot(GameConfig.botDifficulty, GameConfig.gameVariant)
            // If bot starts first (random 50/50), trigger bot move
            if (_state.value.currentPlayerId == 2) {
                viewModelScope.launch { executeBotMove() }
            }
        }
    }

    private fun createInitialState(): GameUiState {
        val config = GameConfig
        val playersList = config.getPlayers()
        // In VS_BOT mode, randomly decide who starts (50/50)
        val startingPlayer = if (config.gameMode == GameMode.VS_BOT) {
            if (kotlin.random.Random.nextBoolean()) 1 else 2
        } else {
            1
        }
        return GameUiState(
            board = engine.createEmptyBoard(config.gridSize),
            gridSize = config.gridSize,
            currentPlayerId = startingPlayer,
            players = playersList,
            numPlayers = playersList.size,
            gameMode = config.gameMode,
            gameVariant = config.gameVariant,
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

        moveJob = viewModelScope.launch {
            executeMove(row, col)
        }
    }

    /**
     * Suspends if the game is currently paused (settings open).
     * Resumes when unpaused. Also checks for cancellation.
     */
    private suspend fun awaitIfPaused() {
        kotlin.coroutines.coroutineContext.ensureActive()
        if (_state.value.isPaused) {
            pauseMutex.withLock { /* blocks until unlocked by resumeGame() */ }
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
            val firstMoveDots = if (engine.isClassic) 1 else 3
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = firstMoveDots, previousDots = 0)
        } else {
            val currentCell = intermediateBoard[row][col]
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = currentCell.dots + 1, previousDots = currentCell.dots)
        }
        _state.update { it.copy(board = intermediateBoard.map { r -> r.toList() }) }

        // Wait for dot transition animation (250ms)
        delay(250)
        awaitIfPaused()

        // Animate explosions wave by wave (BFS)
        for (waveData in explosionWaveData) {
            // Pause before each split to show the critical-mass state
            delay(200)
            awaitIfPaused()

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
            awaitIfPaused()

            // Phase 2: Show board after dots arrived at neighbors
            _state.update {
                it.copy(
                    board = waveData.boardAfterSplit,
                    explodingCells = emptySet(),
                    explosionMoves = emptyList()
                )
            }
            // Allow dot transition animation to play (for newly formed cells)
            delay(250)
            awaitIfPaused()
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

        // Determine next player: cycle through active players (skip eliminated & stuck ones)
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
     * Also checks that the candidate actually has valid moves available.
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
            if (!hasMoved || hasCells) {
                // Verify they actually have valid moves
                val isFirstMove = !hasMoved
                val validMoves = engine.getValidMoves(board, candidate, isFirstMove)
                if (validMoves.isNotEmpty()) return candidate
                // If no valid moves, skip this player
            }
        }
        // Fallback: return current player (game should end via win condition)
        return currentPlayerId
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
        // Cancel any in-flight move/animation coroutine before resetting
        moveJob?.cancel()
        moveJob = null
        // Release pause lock if held
        if (pauseMutex.isLocked) {
            try { pauseMutex.unlock() } catch (_: Exception) {}
        }
        _state.value = createInitialState()
    }

    /** Called when navigating to settings — pauses animation processing */
    fun pauseGame() {
        if (!pauseMutex.isLocked) {
            pauseMutex.tryLock()
        }
        _state.update { it.copy(isPaused = true) }
    }

    /** Called when returning from settings — resumes animation processing */
    fun resumeGame() {
        _state.update { it.copy(isPaused = false) }
        if (pauseMutex.isLocked) {
            try { pauseMutex.unlock() } catch (_: Exception) {}
        }
    }
}
