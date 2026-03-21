package com.blueedge.chainreaction.bridge

import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.OnlineGameRepository
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.RoomState
import com.blueedge.chainreaction.platform.OnlineGameRepo
import kotlinx.coroutines.flow.Flow

/** Bridges the Android OnlineGameRepository (Firebase) to the shared OnlineGameRepo interface. */
object AndroidOnlineGameRepo : OnlineGameRepo {
    override suspend fun ensureAuthenticated(): String = OnlineGameRepository.ensureAuthenticated()
    override fun currentUid(): String = OnlineGameRepository.currentUid()
    override suspend fun createRoom(gridSize: Int, gameVariant: GameVariant, hostName: String, hostColorIndex: Int): String =
        OnlineGameRepository.createRoom(gridSize, gameVariant, hostName, hostColorIndex)
    override suspend fun joinRoom(roomCode: String, guestName: String, guestColorIndex: Int): Boolean =
        OnlineGameRepository.joinRoom(roomCode, guestName, guestColorIndex)
    override suspend fun findRandomMatch(gridSize: Int, gameVariant: GameVariant, playerName: String, playerColorIndex: Int): String =
        OnlineGameRepository.findRandomMatch(gridSize, gameVariant, playerName, playerColorIndex)
    override suspend fun sendMove(roomCode: String, row: Int, col: Int, playerId: Int) =
        OnlineGameRepository.sendMove(roomCode, row, col, playerId)
    override suspend fun syncGameState(
        roomCode: String,
        board: List<List<CellState>>,
        currentPlayerId: Int,
        moveCount: Int,
        winnerId: Int,
        gameStatus: GameStatus
    ) = OnlineGameRepository.syncGameState(roomCode, board, currentPlayerId, moveCount, winnerId, gameStatus)
    override fun listenToRoom(roomCode: String): Flow<RoomState> = OnlineGameRepository.listenToRoom(roomCode)
    override suspend fun leaveRoom(roomCode: String) = OnlineGameRepository.leaveRoom(roomCode)
    override fun deserializeBoard(serialized: List<String>, gridSize: Int): List<List<CellState>> =
        OnlineGameRepository.deserializeBoard(serialized, gridSize)
}
