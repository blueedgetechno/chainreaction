package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.createBot
import com.blueedge.chainreaction.platform.ServiceLocator
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameUiState
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.domain.GameEngine
import com.blueedge.chainreaction.platform.currentTimeMillis
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

    // Undo: snapshot of state before the last human move
    private var undoSnapshot: GameUiState? = null

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val sound get() = ServiceLocator.soundPlayer

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
            gameStartTimeMs = currentTimeMillis()
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

        // Save snapshot for undo before any changes (only for human moves)
        val isHumanMove = !(currentState.gameMode == GameMode.VS_BOT && playerId == 2)
        if (isHumanMove) {
            undoSnapshot = currentState.copy(
                isAnimating = false,
                lastMovedCell = null,
                explodingCells = emptySet(),
                explosionMoves = emptyList()
            )
        }

        sound.playBop()

        val (newBoard, explosionWaveData) = engine.executeMove(currentState.board, row, col, playerId, isFirstMove)

        // Show the intermediate board state
        val intermediateBoard = currentState.board.map { it.toMutableList() }.toMutableList()
        if (isFirstMove) {
            val firstMoveDots = if (engine.isClassic) 1 else 3
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = firstMoveDots, previousDots = 0)
        } else {
            val currentCell = intermediateBoard[row][col]
            intermediateBoard[row][col] = CellState(ownerId = playerId, dots = currentCell.dots + 1, previousDots = currentCell.dots)
        }
        _state.update { it.copy(board = intermediateBoard.map { r -> r.toList() }) }

        delay(250)
        awaitIfPaused()

        // Animate explosions wave by wave
        for (waveData in explosionWaveData) {
            delay(200)
            awaitIfPaused()

            sound.playPop()
            sound.vibrate()

            _state.update {
                it.copy(
                    board = waveData.boardBeforeSplit,
                    explodingCells = waveData.explodingCells.map { m -> Pair(m.row, m.col) }.toSet(),
                    explosionMoves = waveData.moves
                )
            }
            delay(300)
            awaitIfPaused()

            _state.update {
                it.copy(
                    board = waveData.boardAfterSplit,
                    explodingCells = emptySet(),
                    explosionMoves = emptyList()
                )
            }
            delay(250)
            awaitIfPaused()
        }

        _state.update { it.copy(explodingCells = emptySet(), explosionMoves = emptyList()) }

        val newMoveCount = currentState.moveCount + 1
        val capturedCells = newBoard.sumOf { row -> row.count { it.dots > 0 } }
        val newPlayersHasMoved = currentState.playersHasMoved + playerId

        val winner = if (newPlayersHasMoved.size >= currentState.numPlayers) {
            engine.checkWinCondition(newBoard, newMoveCount)
        } else {
            null
        }
        val gameStatus = if (winner != null) GameStatus.GAME_OVER else GameStatus.IN_PROGRESS

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
                lastMovedCell = null,
                canUndo = undoSnapshot != null && gameStatus == GameStatus.IN_PROGRESS
            )
        }

        if (gameStatus == GameStatus.IN_PROGRESS &&
            _state.value.gameMode == GameMode.VS_BOT &&
            nextPlayer == 2
        ) {
            executeBotMove()
        }
    }

    private fun getNextActivePlayer(
        board: List<List<CellState>>,
        currentPlayerId: Int,
        players: List<PlayerInfo>,
        playersHasMoved: Set<Int>
    ): Int {
        val numPlayers = players.size
        for (offset in 1..numPlayers) {
            val candidate = ((currentPlayerId - 1 + offset) % numPlayers) + 1
            val hasMoved = playersHasMoved.contains(candidate)
            val hasCells = board.any { row -> row.any { it.ownerId == candidate } }
            if (!hasMoved || hasCells) {
                val isFirstMove = !hasMoved
                val validMoves = engine.getValidMoves(board, candidate, isFirstMove)
                if (validMoves.isNotEmpty()) return candidate
            }
        }
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
        return (currentTimeMillis() - _state.value.gameStartTimeMs) / 1000
    }

    fun undo() {
        val snapshot = undoSnapshot ?: return
        if (_state.value.isAnimating || _state.value.botThinking) return

        moveJob?.cancel()
        moveJob = null

        _state.value = snapshot.copy(canUndo = false)
        undoSnapshot = null
    }

    fun resetGame() {
        moveJob?.cancel()
        moveJob = null
        if (pauseMutex.isLocked) {
            try { pauseMutex.unlock() } catch (_: Exception) {}
        }
        undoSnapshot = null
        _state.value = createInitialState()
    }

    fun pauseGame() {
        if (!pauseMutex.isLocked) {
            pauseMutex.tryLock()
        }
        _state.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _state.update { it.copy(isPaused = false) }
        if (pauseMutex.isLocked) {
            try { pauseMutex.unlock() } catch (_: Exception) {}
        }
    }
}
