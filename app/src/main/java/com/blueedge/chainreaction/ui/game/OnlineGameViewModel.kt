package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameUiState
import com.blueedge.chainreaction.data.OnlineGameRepository
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.data.RoomStatus
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

class OnlineGameViewModel : ViewModel() {

    private val engine = GameEngine(GameConfig.gameVariant)
    private var moveJob: Job? = null
    private var roomListenerJob: Job? = null
    private val pauseMutex = Mutex()

    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    /** Track the last move timestamp we've already processed to avoid replaying it. */
    private var lastProcessedMoveTimestamp: Long = 0

    var roomCode: String = ""
        private set

    private fun createInitialState(): GameUiState {
        val config = GameConfig
        val playersList = config.getPlayers()
        return GameUiState(
            board = engine.createEmptyBoard(config.gridSize),
            gridSize = config.gridSize,
            currentPlayerId = 1,
            players = playersList,
            numPlayers = 2,
            gameMode = GameMode.ONLINE_MULTIPLAYER,
            gameVariant = config.gameVariant,
            gameStartTimeMs = System.currentTimeMillis(),
            waitingForOpponent = true
        )
    }

    // ── Room Lifecycle ──────────────────────────────────────────────

    fun createRoom() {
        viewModelScope.launch {
            val code = OnlineGameRepository.createRoom()
            roomCode = code
            _state.update { it.copy(roomCode = code, isHost = true, localPlayerId = 1, waitingForOpponent = true) }
            startListeningToRoom(code)
        }
    }

    fun joinRoom(code: String) {
        viewModelScope.launch {
            val success = OnlineGameRepository.joinRoom(code)
            if (success) {
                roomCode = code
                _state.update { it.copy(roomCode = code, isHost = false, localPlayerId = 2, waitingForOpponent = false) }
                startListeningToRoom(code)
            }
        }
    }

    fun findRandomMatch() {
        viewModelScope.launch {
            _state.update { it.copy(waitingForOpponent = true) }
            val code = OnlineGameRepository.findRandomMatch()
            roomCode = code
            // After findRandomMatch, check if we're host or guest
            val uid = OnlineGameRepository.currentUid()
            val isHost = code.isNotEmpty() // Will be determined by room listener
            _state.update { it.copy(roomCode = code, waitingForOpponent = true) }
            startListeningToRoom(code)
        }
    }

    private fun startListeningToRoom(code: String) {
        roomListenerJob?.cancel()
        roomListenerJob = viewModelScope.launch {
            OnlineGameRepository.listenToRoom(code).collect { roomState ->
                handleRoomUpdate(roomState)
            }
        }
    }

    private fun handleRoomUpdate(room: com.blueedge.chainreaction.data.RoomState) {
        val uid = OnlineGameRepository.currentUid()
        val isHost = room.hostUid == uid
        val localPlayerId = if (isHost) 1 else 2

        // Determine if opponent joined
        val opponentJoined = room.status == RoomStatus.IN_PROGRESS.name
        val opponentDisconnected = if (isHost) {
            room.guestUid == "disconnected"
        } else {
            room.status == RoomStatus.FINISHED.name
        }

        // Build player list from room data
        val players = listOf(
            PlayerInfo(1, room.hostName, room.hostColorIndex),
            PlayerInfo(2, room.guestName, room.guestColorIndex)
        )

        val opponentName = if (isHost) room.guestName else room.hostName

        // Check if there's a new remote move to process
        if (room.lastMoveBy != 0 &&
            room.lastMoveBy != localPlayerId &&
            room.lastMoveTimestamp > lastProcessedMoveTimestamp &&
            room.lastMoveRow >= 0 &&
            room.lastMoveCol >= 0
        ) {
            lastProcessedMoveTimestamp = room.lastMoveTimestamp
            // Execute the remote player's move locally for animation
            moveJob = viewModelScope.launch {
                executeMove(room.lastMoveRow, room.lastMoveCol, isRemote = true)
            }
        }

        // Update non-move state
        _state.update { state ->
            state.copy(
                players = players,
                isHost = isHost,
                localPlayerId = localPlayerId,
                roomCode = room.roomCode,
                opponentName = opponentName,
                waitingForOpponent = !opponentJoined,
                opponentDisconnected = opponentDisconnected
            )
        }
    }

    // ── Move Handling ───────────────────────────────────────────────

    fun onCellClicked(row: Int, col: Int) {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.IN_PROGRESS) return
        if (currentState.isAnimating) return
        if (currentState.waitingForOpponent) return

        // Only allow clicks when it's the local player's turn
        if (currentState.currentPlayerId != currentState.localPlayerId) return

        val isFirstMove = !currentState.playersHasMoved.contains(currentState.currentPlayerId)
        if (!engine.isValidMove(currentState.board, row, col, currentState.currentPlayerId, isFirstMove)) return

        // Send the move to Firebase first, then execute locally
        moveJob = viewModelScope.launch {
            OnlineGameRepository.sendMove(roomCode, row, col, currentState.currentPlayerId)
            executeMove(row, col, isRemote = false)
        }
    }

    private suspend fun awaitIfPaused() {
        kotlin.coroutines.coroutineContext.ensureActive()
        if (_state.value.isPaused) {
            pauseMutex.withLock { }
        }
    }

    private suspend fun executeMove(row: Int, col: Int, isRemote: Boolean) {
        val currentState = _state.value
        val playerId = currentState.currentPlayerId
        val isFirstMove = !currentState.playersHasMoved.contains(playerId)

        _state.update { it.copy(isAnimating = true, lastMovedCell = Pair(row, col)) }

        SoundManager.playBop()

        val (newBoard, explosionWaveData) = engine.executeMove(currentState.board, row, col, playerId, isFirstMove)

        // Show intermediate board state for dot transition
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

            SoundManager.playPop()
            SoundManager.vibrate()

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
        val capturedCells = newBoard.sumOf { r -> r.count { it.dots > 0 } }
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
                canUndo = false // No undo in online mode
            )
        }

        // If this was a local move, sync the final state to Firebase
        if (!isRemote) {
            val finalState = _state.value
            viewModelScope.launch {
                OnlineGameRepository.syncGameState(
                    roomCode = roomCode,
                    board = finalState.board,
                    currentPlayerId = finalState.currentPlayerId,
                    moveCount = finalState.moveCount,
                    winnerId = finalState.winnerId,
                    gameStatus = finalState.gameStatus
                )
            }
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

    fun getGameDurationSeconds(): Long {
        return (System.currentTimeMillis() - _state.value.gameStartTimeMs) / 1000
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

    fun leaveRoom() {
        roomListenerJob?.cancel()
        moveJob?.cancel()
        viewModelScope.launch {
            if (roomCode.isNotEmpty()) {
                OnlineGameRepository.leaveRoom(roomCode)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
    }
}
