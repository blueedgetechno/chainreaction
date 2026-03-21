package com.blueedge.chainreaction.platform

import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.RoomState
import kotlinx.coroutines.flow.Flow

/** Abstraction for online multiplayer backend (Firebase on Android, stub on iOS). */
interface OnlineGameRepo {
    suspend fun ensureAuthenticated(): String
    fun currentUid(): String
    suspend fun createRoom(
        gridSize: Int, gameVariant: GameVariant,
        hostName: String, hostColorIndex: Int
    ): String
    suspend fun joinRoom(roomCode: String, guestName: String, guestColorIndex: Int): Boolean
    suspend fun findRandomMatch(
        gridSize: Int, gameVariant: GameVariant,
        playerName: String, playerColorIndex: Int
    ): String
    suspend fun sendMove(roomCode: String, row: Int, col: Int, playerId: Int)
    suspend fun syncGameState(
        roomCode: String, board: List<List<CellState>>,
        currentPlayerId: Int, moveCount: Int,
        winnerId: Int, gameStatus: GameStatus
    )
    fun listenToRoom(roomCode: String): Flow<RoomState>
    suspend fun leaveRoom(roomCode: String)
    fun deserializeBoard(serialized: List<String>, gridSize: Int): List<List<CellState>>
}

/** Stub for platforms where online multiplayer is not available. */
object NoOpOnlineGameRepo : OnlineGameRepo {
    override suspend fun ensureAuthenticated(): String = ""
    override fun currentUid(): String = ""
    override suspend fun createRoom(gridSize: Int, gameVariant: GameVariant, hostName: String, hostColorIndex: Int): String = ""
    override suspend fun joinRoom(roomCode: String, guestName: String, guestColorIndex: Int): Boolean = false
    override suspend fun findRandomMatch(gridSize: Int, gameVariant: GameVariant, playerName: String, playerColorIndex: Int): String = ""
    override suspend fun sendMove(roomCode: String, row: Int, col: Int, playerId: Int) {}
    override suspend fun syncGameState(roomCode: String, board: List<List<CellState>>, currentPlayerId: Int, moveCount: Int, winnerId: Int, gameStatus: GameStatus) {}
    override fun listenToRoom(roomCode: String): Flow<RoomState> = kotlinx.coroutines.flow.emptyFlow()
    override suspend fun leaveRoom(roomCode: String) {}
    override fun deserializeBoard(serialized: List<String>, gridSize: Int): List<List<CellState>> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.chunked(gridSize).map { row ->
            row.map { cell ->
                val parts = cell.split(":")
                CellState(
                    ownerId = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    dots = parts.getOrNull(1)?.toIntOrNull() ?: 0
                )
            }
        }
    }
}
