package com.blueedge.chainreaction.platform

import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameStatus
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.RoomState
import com.blueedge.chainreaction.data.RoomStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Implements [OnlineGameRepo] for iOS by wrapping a [FirebaseBridge] (implemented in Swift).
 * Converts callback-based bridge calls to coroutines and Flows.
 */
class IosOnlineGameRepo(private val bridge: FirebaseBridge) : OnlineGameRepo {

    override suspend fun ensureAuthenticated(): String = suspendCoroutine { cont ->
        bridge.ensureAuth(
            onSuccess = { uid -> cont.resume(uid) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override fun currentUid(): String = bridge.getUid()

    override suspend fun createRoom(
        gridSize: Int, gameVariant: GameVariant,
        hostName: String, hostColorIndex: Int
    ): String = suspendCoroutine { cont ->
        bridge.createRoom(
            gridSize = gridSize,
            gameVariant = gameVariant.name,
            hostName = hostName,
            hostColorIndex = hostColorIndex,
            onSuccess = { code -> cont.resume(code) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override suspend fun joinRoom(
        roomCode: String, guestName: String, guestColorIndex: Int
    ): Boolean = suspendCoroutine { cont ->
        bridge.joinRoom(
            roomCode = roomCode,
            guestName = guestName,
            guestColorIndex = guestColorIndex,
            onSuccess = { success -> cont.resume(success) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override suspend fun findRandomMatch(
        gridSize: Int, gameVariant: GameVariant,
        playerName: String, playerColorIndex: Int
    ): String = suspendCoroutine { cont ->
        bridge.findRandomMatch(
            gridSize = gridSize,
            gameVariant = gameVariant.name,
            playerName = playerName,
            playerColorIndex = playerColorIndex,
            onSuccess = { code -> cont.resume(code) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override suspend fun sendMove(
        roomCode: String, row: Int, col: Int, playerId: Int
    ): Unit = suspendCoroutine { cont ->
        bridge.sendMove(
            roomCode = roomCode, row = row, col = col, playerId = playerId,
            onSuccess = { cont.resume(Unit) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override suspend fun syncGameState(
        roomCode: String, board: List<List<CellState>>,
        currentPlayerId: Int, moveCount: Int,
        winnerId: Int, gameStatus: GameStatus
    ): Unit = suspendCoroutine { cont ->
        val serializedBoard = board.flatMap { row ->
            row.map { cell -> "${cell.ownerId}:${cell.dots}" }
        }
        bridge.syncGameState(
            roomCode = roomCode,
            board = serializedBoard,
            currentPlayerId = currentPlayerId,
            moveCount = moveCount,
            winnerId = winnerId,
            gameStatus = gameStatus.name,
            onSuccess = { cont.resume(Unit) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

    override fun listenToRoom(roomCode: String): Flow<RoomState> = callbackFlow {
        bridge.listenToRoom(
            roomCode = roomCode,
            onUpdate = { data ->
                @Suppress("UNCHECKED_CAST")
                val room = RoomState(
                    roomCode = roomCode,
                    hostUid = data["hostUid"] as? String ?: "",
                    guestUid = data["guestUid"] as? String ?: "",
                    status = data["status"] as? String ?: RoomStatus.WAITING.name,
                    gridSize = (data["gridSize"] as? Number)?.toInt() ?: 6,
                    gameVariant = data["gameVariant"] as? String ?: GameVariant.CLASSIC.name,
                    hostName = data["hostName"] as? String ?: "Player 1",
                    guestName = data["guestName"] as? String ?: "Player 2",
                    hostColorIndex = (data["hostColorIndex"] as? Number)?.toInt() ?: 0,
                    guestColorIndex = (data["guestColorIndex"] as? Number)?.toInt() ?: 1,
                    board = (data["board"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    currentPlayerId = (data["currentPlayerId"] as? Number)?.toInt() ?: 1,
                    moveCount = (data["moveCount"] as? Number)?.toInt() ?: 0,
                    winnerId = (data["winnerId"] as? Number)?.toInt() ?: 0,
                    gameStatus = data["gameStatus"] as? String ?: GameStatus.IN_PROGRESS.name,
                    lastMoveRow = (data["lastMoveRow"] as? Number)?.toInt() ?: -1,
                    lastMoveCol = (data["lastMoveCol"] as? Number)?.toInt() ?: -1,
                    lastMoveBy = (data["lastMoveBy"] as? Number)?.toInt() ?: 0,
                    lastMoveTimestamp = (data["lastMoveTimestamp"] as? Number)?.toLong() ?: 0
                )
                trySend(room)
            },
            onError = { err ->
                close(Exception(err))
            }
        )
        awaitClose { bridge.stopListening(roomCode) }
    }

    override suspend fun leaveRoom(roomCode: String): Unit = suspendCoroutine { cont ->
        bridge.leaveRoom(
            roomCode = roomCode,
            onSuccess = { cont.resume(Unit) },
            onError = { err -> cont.resumeWithException(Exception(err)) }
        )
    }

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
