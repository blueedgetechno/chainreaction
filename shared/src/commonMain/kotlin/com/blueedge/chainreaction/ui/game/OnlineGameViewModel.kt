package com.blueedge.chainreaction.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.createBot
import com.blueedge.chainreaction.platform.ServiceLocator
import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameUiState
import com.blueedge.chainreaction.data.PlayerInfo
import com.blueedge.chainreaction.data.RoomStatus
import com.blueedge.chainreaction.data.Strings
import com.blueedge.chainreaction.domain.GameEngine
import com.blueedge.chainreaction.platform.currentTimeMillis
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

    private var lastProcessedMoveTimestamp: Long = 0

    var roomCode: String = ""
        private set

    private val sound get() = ServiceLocator.soundPlayer
    private val repo get() = ServiceLocator.onlineGameRepo

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
            gameStartTimeMs = currentTimeMillis(),
            waitingForOpponent = true
        )
    }

    // ── Room Lifecycle ──────────────────────────────────────────────

    fun createRoom() {
        viewModelScope.launch {
            val code = repo.createRoom(
                gridSize = GameConfig.gridSize,
                gameVariant = GameConfig.gameVariant,
                hostName = Strings.colorName(GameConfig.player1ColorIndex),
                hostColorIndex = GameConfig.player1ColorIndex
            )
            roomCode = code
            _state.update { it.copy(roomCode = code, isHost = true, localPlayerId = 1, waitingForOpponent = true) }
            startListeningToRoom(code)
        }
    }

    fun joinRoom(code: String) {
        viewModelScope.launch {
            val success = repo.joinRoom(
                code,
                guestName = Strings.colorName(GameConfig.player1ColorIndex),
                guestColorIndex = GameConfig.player1ColorIndex
            )
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
            val code = repo.findRandomMatch(
                gridSize = GameConfig.gridSize,
                gameVariant = GameConfig.gameVariant,
                playerName = Strings.colorName(GameConfig.player1ColorIndex),
                playerColorIndex = GameConfig.player1ColorIndex
            )
            roomCode = code
            val uid = repo.currentUid()
            _state.update { it.copy(roomCode = code, waitingForOpponent = true) }
            startListeningToRoom(code)
        }
    }

    fun attachToRoom(code: String) {
        roomCode = code
        _state.update { it.copy(roomCode = code, waitingForOpponent = false) }
        startListeningToRoom(code)
    }

    private fun startListeningToRoom(code: String) {
        roomListenerJob?.cancel()
        roomListenerJob = viewModelScope.launch {
            repo.listenToRoom(code).collect { roomState ->
                handleRoomUpdate(roomState)
            }
        }
    }

    private fun handleRoomUpdate(room: com.blueedge.chainreaction.data.RoomState) {
        val uid = repo.currentUid()
        val isHost = room.hostUid == uid
        val localPlayerId = if (isHost) 1 else 2

        val opponentJoined = room.status == RoomStatus.IN_PROGRESS.name
        val opponentDisconnected = room.status == RoomStatus.FINISHED.name &&
                _state.value.gameStatus == GameStatus.IN_PROGRESS

        val players = listOf(
            PlayerInfo(1, room.hostName, room.hostColorIndex),
            PlayerInfo(2, room.guestName, room.guestColorIndex)
        )

        val opponentColorIndex = if (isHost) room.guestColorIndex else room.hostColorIndex
        val opponentName = Strings.colorName(opponentColorIndex)

        if (room.lastMoveBy != 0 &&
            room.lastMoveBy != localPlayerId &&
            room.lastMoveTimestamp > lastProcessedMoveTimestamp &&
            room.lastMoveRow >= 0 &&
            room.lastMoveCol >= 0
        ) {
            lastProcessedMoveTimestamp = room.lastMoveTimestamp
            moveJob = viewModelScope.launch {
                executeMove(room.lastMoveRow, room.lastMoveCol, isRemote = true)
            }
        }

        if (!_state.value.isAnimating && room.currentPlayerId != 0) {
            val localCurrentPlayer = _state.value.currentPlayerId
            if (room.currentPlayerId != localCurrentPlayer &&
                room.lastMoveTimestamp <= lastProcessedMoveTimestamp
            ) {
                _state.update { it.copy(
                    currentPlayerId = room.currentPlayerId,
                    turnDeadlineMs = currentTimeMillis() + 30_000
                ) }
            }
        }

        if (!_state.value.isAnimating && room.board.isNotEmpty() &&
            room.moveCount > _state.value.moveCount
        ) {
            val remoteBoard = repo.deserializeBoard(room.board, room.gridSize)
            if (remoteBoard.isNotEmpty()) {
                _state.update { it.copy(
                    board = remoteBoard,
                    moveCount = room.moveCount,
                    currentPlayerId = room.currentPlayerId,
                    turnDeadlineMs = currentTimeMillis() + 30_000
                ) }
                lastProcessedMoveTimestamp = room.lastMoveTimestamp
            }
        }

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
                    currentTimeMillis() + 30_000
                } else state.turnDeadlineMs
            )
        }
    }

    // ── Move Handling ───────────────────────────────────────────────

    fun skipTurn() {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.IN_PROGRESS) return

        val nextPlayer = getNextActivePlayer(
            currentState.board, currentState.currentPlayerId, currentState.players, currentState.playersHasMoved
        )
        if (nextPlayer == currentState.currentPlayerId) return

        _state.update { it.copy(
            currentPlayerId = nextPlayer,
            turnDeadlineMs = if (!isBotMode) currentTimeMillis() + 30_000 else 0L
        ) }

        if (!isBotMode && currentState.currentPlayerId == currentState.localPlayerId) {
            viewModelScope.launch {
                repo.syncGameState(
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

        if (currentState.currentPlayerId != currentState.localPlayerId) return

        val isFirstMove = !currentState.playersHasMoved.contains(currentState.currentPlayerId)
        if (!engine.isValidMove(currentState.board, row, col, currentState.currentPlayerId, isFirstMove)) return

        moveJob = viewModelScope.launch {
            if (!isBotMode) {
                repo.sendMove(roomCode, row, col, currentState.currentPlayerId)
            }
            executeMove(row, col, isRemote = false)
        }
    }

    private suspend fun executeMove(row: Int, col: Int, isRemote: Boolean) {
        val currentState = _state.value
        val playerId = currentState.currentPlayerId
        val isFirstMove = !currentState.playersHasMoved.contains(playerId)

        _state.update { it.copy(isAnimating = true, lastMovedCell = Pair(row, col)) }

        sound.playBop()

        val (newBoard, explosionWaveData) = engine.executeMove(currentState.board, row, col, playerId, isFirstMove)

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

        for (waveData in explosionWaveData) {
            delay(200)

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
            currentTimeMillis() + 30_000
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
                canUndo = false,
                turnDeadlineMs = newDeadline
            )
        }

        if (!isRemote && !isBotMode) {
            val finalState = _state.value
            viewModelScope.launch {
                repo.syncGameState(
                    roomCode = roomCode,
                    board = finalState.board,
                    currentPlayerId = finalState.currentPlayerId,
                    moveCount = finalState.moveCount,
                    winnerId = finalState.winnerId,
                    gameStatus = finalState.gameStatus
                )
            }
        }

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
        return (currentTimeMillis() - _state.value.gameStartTimeMs) / 1000
    }

    fun switchToBot() {
        roomListenerJob?.cancel()
        isBotMode = true
        botStrategy = createBot(BotDifficulty.MEDIUM, GameConfig.gameVariant)

        _state.update { it.copy(opponentDisconnected = false, isBotMode = true, turnDeadlineMs = 0L) }

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
            viewModelScope.launch {
                repo.leaveRoom(roomCode)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
    }
}
