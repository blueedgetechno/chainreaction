package com.blueedge.chainreaction.data

enum class GameMode {
    LOCAL_MULTIPLAYER,
    VS_BOT,
    ONLINE_MULTIPLAYER
}

enum class GameVariant {
    SIMPLE,
    CLASSIC
}

enum class BotDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class PlayerInfo(
    val id: Int,
    val name: String,
    val colorIndex: Int,
    val isBot: Boolean = false
)

data class CellState(
    val ownerId: Int = 0,
    val dots: Int = 0,
    val previousDots: Int = 0
) {
    val isEmpty: Boolean get() = ownerId == 0 && dots == 0
}

data class Move(val row: Int, val col: Int)

data class ExplosionMove(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val playerId: Int
)

data class ExplosionWaveData(
    val explodingCells: List<Move>,
    val moves: List<ExplosionMove>,
    val boardBeforeSplit: List<List<CellState>>,
    val boardAfterSplit: List<List<CellState>>
)

enum class GameStatus {
    IN_PROGRESS,
    GAME_OVER
}

data class GameUiState(
    val board: List<List<CellState>> = emptyList(),
    val gridSize: Int = 6,
    val currentPlayerId: Int = 1,
    val players: List<PlayerInfo> = listOf(PlayerInfo(1, "Player 1", 0), PlayerInfo(2, "Player 2", 1)),
    val numPlayers: Int = 2,
    val capturedCells: Int = 0,
    val moveCount: Int = 0,
    val playersHasMoved: Set<Int> = emptySet(),
    val winnerId: Int = 0,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val isAnimating: Boolean = false,
    val botThinking: Boolean = false,
    val gameStartTimeMs: Long = 0L,
    val explodingCells: Set<Pair<Int, Int>> = emptySet(),
    val explosionMoves: List<ExplosionMove> = emptyList(),
    val lastMovedCell: Pair<Int, Int>? = null,
    val isPaused: Boolean = false,
    val canUndo: Boolean = false,
    val gameMode: GameMode = GameMode.LOCAL_MULTIPLAYER,
    val gameVariant: GameVariant = GameVariant.SIMPLE,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    // Online multiplayer fields
    val roomCode: String = "",
    val localPlayerId: Int = 1,
    val isHost: Boolean = false,
    val opponentName: String = "",
    val waitingForOpponent: Boolean = false,
    val opponentDisconnected: Boolean = false,
    val isBotMode: Boolean = false,
    val turnDeadlineMs: Long = 0L
)

/** Lobby status for a room. */
enum class RoomStatus { WAITING, IN_PROGRESS, FINISHED }

/** Represents the state of an online room as stored in Firebase RTDB. */
data class RoomState(
    val roomCode: String = "",
    val hostUid: String = "",
    val guestUid: String = "",
    val status: String = RoomStatus.WAITING.name,
    val gridSize: Int = 6,
    val gameVariant: String = GameVariant.CLASSIC.name,
    val hostName: String = "Player 1",
    val guestName: String = "Player 2",
    val hostColorIndex: Int = 0,
    val guestColorIndex: Int = 1,
    val board: List<String> = emptyList(),
    val currentPlayerId: Int = 1,
    val moveCount: Int = 0,
    val winnerId: Int = 0,
    val gameStatus: String = GameStatus.IN_PROGRESS.name,
    val lastMoveRow: Int = -1,
    val lastMoveCol: Int = -1,
    val lastMoveBy: Int = 0,
    val lastMoveTimestamp: Long = 0
)
