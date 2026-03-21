package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.createBot
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameUiState
import com.blueedge.chainreaction.data.OnlineGameRepository
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.data.RoomStatus
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.domain.GameEngine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnlineGameViewModel : ViewModel() {

    private val engine = GameEngine(GameConfig.gameVariant)
    private var moveJob: Job? = null
    private var roomListenerJob: Job? = null
    private var botStrategy: BotStrategy? = null
    private var isBotMode = false

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

    /** Attach to an already-matched room (lobby already did findRandomMatch). */
    fun attachToRoom(code: String) {
        roomCode = code
        _state.update { it.copy(roomCode = code, waitingForOpponent = false) }
        startListeningToRoom(code)
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
        val opponentDisconnected = room.status == RoomStatus.FINISHED.name &&
                _state.value.gameStatus == GameStatus.IN_PROGRESS

        // Build player list from room data
        val players = listOf(
            PlayerInfo(1, room.hostName, room.hostColorIndex),
            PlayerInfo(2, room.guestName, room.guestColorIndex)
        )

        val opponentColorIndex = if (isHost) room.guestColorIndex else room.hostColorIndex
        val opponentName = Strings.colorName(opponentColorIndex)

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

        // Sync currentPlayerId from Firebase when no move is in progress
        // This handles turn skips synced by the other player
        if (!_state.value.isAnimating && room.currentPlayerId != 0) {
            val localCurrentPlayer = _state.value.currentPlayerId
            if (room.currentPlayerId != localCurrentPlayer &&
                room.lastMoveTimestamp <= lastProcessedMoveTimestamp
            ) {
                _state.update { it.copy(
                    currentPlayerId = room.currentPlayerId,
                    turnDeadlineMs = System.currentTimeMillis() + 30_000
                ) }
            }
        }

        // Sync board from Firebase for recovery — if Firebase has a newer board
        // and no animation is in progress, adopt it as truth
        if (!_state.value.isAnimating && room.board.isNotEmpty() &&
            room.moveCount > _state.value.moveCount
        ) {
            val remoteBoard = OnlineGameRepository.deserializeBoard(room.board, room.gridSize)
            if (remoteBoard.isNotEmpty()) {
                _state.update { it.copy(
                    board = remoteBoard,
                    moveCount = room.moveCount,
                    currentPlayerId = room.currentPlayerId,
                    turnDeadlineMs = System.currentTimeMillis() + 30_000
                ) }
                lastProcessedMoveTimestamp = room.lastMoveTimestamp
            }
        }

        // Update non-move state – never show "waiting" once the game is over
        val newWaiting = if (opponentDisconnected || _state.value.gameStatus == GameStatus.GAME_OVER) false else !opponentJoined
        _state.update { state ->
            state.copy(
                players = players,
                isHost = isHost,
                localPlayerId = localPlayerId,
                roomCode = room.roomCode,
                opponentName = opponentName,
                waitingForOpponent = newWaiting,
                opponentDisconnected = opponentDisconnected,
                turnDeadlineMs = if (!newWaiting && state.turnDeadlineMs == 0L && state.gameStatus == GameStatus.IN_PROGRESS) {
                    System.currentTimeMillis() + 30_000
                } else state.turnDeadlineMs
            )
        }
    }

    // ── Move Handling ───────────────────────────────────────────────

    /** Skip the current player's turn (called when 30s timer expires). */
    fun skipTurn() {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.IN_PROGRESS) return

        val nextPlayer = getNextActivePlayer(
            currentState.board, currentState.currentPlayerId, currentState.players, currentState.playersHasMoved
        )
        // Don't skip if next player is the same (e.g. only one player left)
        if (nextPlayer == currentState.currentPlayerId) return

        _state.update { it.copy(
            currentPlayerId = nextPlayer,
            turnDeadlineMs = if (!isBotMode) System.currentTimeMillis() + 30_000 else 0L
        ) }

        // Only the player whose turn was skipped syncs to Firebase
        if (!isBotMode && currentState.currentPlayerId == currentState.localPlayerId) {
            viewModelScope.launch {
                OnlineGameRepository.syncGameState(
                    roomCode = roomCode,
                    board = currentState.board,
                    currentPlayerId = nextPlayer,
                    moveCount = currentState.moveCount,
                    winnerId = currentState.winnerId,
                    gameStatus = currentState.gameStatus
                )
            }
        }
    }

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
            if (!isBotMode) {
                OnlineGameRepository.sendMove(roomCode, row, col, currentState.currentPlayerId)
            }
            executeMove(row, col, isRemote = false)
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

        // Animate explosions wave by wave
        for (waveData in explosionWaveData) {
            delay(200)

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

            _state.update {
                it.copy(
                    board = waveData.boardAfterSplit,
                    explodingCells = emptySet(),
                    explosionMoves = emptyList()
                )
            }
            delay(250)
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

        val newDeadline = if (gameStatus == GameStatus.IN_PROGRESS && !isBotMode) {
            System.currentTimeMillis() + 30_000
        } else 0L

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
                canUndo = false, // No undo in online mode
                turnDeadlineMs = newDeadline
            )
        }

        // If this was a local move, sync the final state to Firebase
        if (!isRemote && !isBotMode) {
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

        // In bot mode, trigger bot move if it's the opponent's turn
        if (isBotMode) {
            val finalState = _state.value
            val opponentId = if (finalState.localPlayerId == 1) 2 else 1
            if (finalState.gameStatus == GameStatus.IN_PROGRESS &&
                finalState.currentPlayerId == opponentId
            ) {
                executeBotMove()
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

    /** Switch from online to local bot when opponent disconnects. */
    fun switchToBot() {
        // Stop listening to Firebase
        roomListenerJob?.cancel()
        isBotMode = true
        botStrategy = createBot(BotDifficulty.MEDIUM, GameConfig.gameVariant)

        // Clear disconnect flag, set bot mode in state, remove timer
        _state.update { it.copy(opponentDisconnected = false, isBotMode = true, turnDeadlineMs = 0L) }

        // If it's currently the opponent's turn, make a bot move
        val currentState = _state.value
        if (currentState.currentPlayerId != currentState.localPlayerId &&
            currentState.gameStatus == GameStatus.IN_PROGRESS
        ) {
            viewModelScope.launch { executeBotMove() }
        }
    }

    private suspend fun executeBotMove() {
        val currentState = _state.value
        val opponentId = if (currentState.localPlayerId == 1) 2 else 1
        val isBotFirstMove = !currentState.playersHasMoved.contains(opponentId)
        val move = botStrategy?.calculateMove(
            currentState.board, opponentId, currentState.localPlayerId, isBotFirstMove
        )
        if (move != null) {
            executeMove(move.row, move.col, isRemote = true)
        }
    }

    fun leaveRoom() {
        roomListenerJob?.cancel()
        moveJob?.cancel()
        if (roomCode.isNotEmpty()) {
            // Use GlobalScope to ensure Firebase update completes even if ViewModel is cleared
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                OnlineGameRepository.leaveRoom(roomCode)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
    }
}
