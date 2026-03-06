package com.blueedge.chainreaction.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
    // Serialized board: flat list of "ownerId:dots" strings, row-major order
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

object OnlineGameRepository {

    private val db = FirebaseDatabase
        .getInstance("https://chainreaction-8e4ec-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val roomsRef = db.getReference("rooms")
    private val matchmakingRef = db.getReference("matchmaking")
    private val auth = FirebaseAuth.getInstance()

    /** Returns current user UID, signing in anonymously if needed. */
    suspend fun ensureAuthenticated(): String {
        val user = auth.currentUser
        if (user != null) return user.uid
        auth.signInAnonymously().await()
        return auth.currentUser?.uid ?: throw IllegalStateException("Auth failed")
    }

    fun currentUid(): String = auth.currentUser?.uid ?: ""

    /** Generate a 6-character uppercase alphanumeric room code. */
    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Omit confusing chars I,O,0,1
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ── Create Room ─────────────────────────────────────────────────

    suspend fun createRoom(
        gridSize: Int = GameConfig.gridSize,
        gameVariant: GameVariant = GameConfig.gameVariant,
        hostName: String = GameConfig.player1Name,
        hostColorIndex: Int = GameConfig.player1ColorIndex
    ): String {
        val uid = ensureAuthenticated()
        // Try codes until we find an unused one (collision is extremely rare with 6 chars)
        var roomCode: String
        var attempts = 0
        do {
            roomCode = generateRoomCode()
            val existing = roomsRef.child(roomCode).get().await()
            attempts++
        } while (existing.exists() && attempts < 5)

        val room = mapOf(
            "hostUid" to uid,
            "guestUid" to "",
            "status" to RoomStatus.WAITING.name,
            "gridSize" to gridSize,
            "gameVariant" to gameVariant.name,
            "hostName" to hostName,
            "hostColorIndex" to hostColorIndex,
            "guestName" to "",
            "guestColorIndex" to 1,
            "currentPlayerId" to 1,
            "moveCount" to 0,
            "winnerId" to 0,
            "gameStatus" to GameStatus.IN_PROGRESS.name,
            "lastMoveRow" to -1,
            "lastMoveCol" to -1,
            "lastMoveBy" to 0,
            "lastMoveTimestamp" to 0,
            "board" to emptyList<String>(),
            "createdAt" to ServerValue.TIMESTAMP
        )

        roomsRef.child(roomCode).setValue(room).await()

        // Set onDisconnect to clean up if host leaves while waiting
        roomsRef.child(roomCode).child("status")
            .onDisconnect().setValue(RoomStatus.FINISHED.name)

        return roomCode
    }

    // ── Join Room ─────────────────────────────────────────────────

    /** Join an existing room. Returns true on success, false if room doesn't exist or is full. */
    suspend fun joinRoom(
        roomCode: String,
        guestName: String = GameConfig.player1Name,
        guestColorIndex: Int = GameConfig.player1ColorIndex
    ): Boolean {
        val uid = ensureAuthenticated()
        val roomRef = roomsRef.child(roomCode)
        val snapshot = roomRef.get().await()

        if (!snapshot.exists()) return false

        val status = snapshot.child("status").getValue(String::class.java) ?: return false
        if (status != RoomStatus.WAITING.name) return false

        val hostUid = snapshot.child("hostUid").getValue(String::class.java) ?: ""
        if (hostUid == uid) return false // Can't join own room

        // Ensure guest color differs from host color
        val hostColorIndex = snapshot.child("hostColorIndex").getValue(Int::class.java) ?: 0
        val actualGuestColor = if (guestColorIndex == hostColorIndex) {
            (guestColorIndex + 1) % 8
        } else {
            guestColorIndex
        }

        val updates = mapOf<String, Any>(
            "guestUid" to uid,
            "guestName" to guestName,
            "guestColorIndex" to actualGuestColor,
            "status" to RoomStatus.IN_PROGRESS.name
        )
        roomRef.updateChildren(updates).await()

        // Cancel host's onDisconnect cleanup since game is starting
        roomRef.child("status").onDisconnect().cancel()

        // Set new onDisconnect for guest
        roomRef.child("guestUid").onDisconnect().setValue("disconnected")

        return true
    }

    // ── Random Matchmaking ─────────────────────────────────────────

    /** Find a random open room or create one. Returns the room code. */
    suspend fun findRandomMatch(
        gridSize: Int = GameConfig.gridSize,
        gameVariant: GameVariant = GameConfig.gameVariant,
        playerName: String = GameConfig.player1Name,
        playerColorIndex: Int = GameConfig.player1ColorIndex
    ): String {
        val uid = ensureAuthenticated()

        // Look for existing waiting rooms (not our own)
        val waitingRooms = roomsRef
            .orderByChild("status")
            .equalTo(RoomStatus.WAITING.name)
            .get().await()

        for (child in waitingRooms.children) {
            val hostUid = child.child("hostUid").getValue(String::class.java) ?: continue
            if (hostUid == uid) continue // Skip our own rooms
            val code = child.key ?: continue

            // Try to join this room
            if (joinRoom(code, playerName, playerColorIndex)) {
                return code
            }
        }

        // No open rooms found — create one and wait
        return createRoom(gridSize, gameVariant, playerName, playerColorIndex)
    }

    // ── Send Move ─────────────────────────────────────────────────

    suspend fun sendMove(roomCode: String, row: Int, col: Int, playerId: Int) {
        val updates = mapOf<String, Any>(
            "lastMoveRow" to row,
            "lastMoveCol" to col,
            "lastMoveBy" to playerId,
            "lastMoveTimestamp" to ServerValue.TIMESTAMP
        )
        roomsRef.child(roomCode).updateChildren(updates).await()
    }

    /** Update the board state and game state in Firebase after processing a move locally. */
    suspend fun syncGameState(
        roomCode: String,
        board: List<List<CellState>>,
        currentPlayerId: Int,
        moveCount: Int,
        winnerId: Int,
        gameStatus: GameStatus
    ) {
        // Serialize board to flat list of "ownerId:dots" strings
        val serializedBoard = board.flatMap { row ->
            row.map { cell -> "${cell.ownerId}:${cell.dots}" }
        }

        val updates = mapOf<String, Any>(
            "board" to serializedBoard,
            "currentPlayerId" to currentPlayerId,
            "moveCount" to moveCount,
            "winnerId" to winnerId,
            "gameStatus" to gameStatus.name
        )
        roomsRef.child(roomCode).updateChildren(updates).await()
    }

    // ── Listen to Room ────────────────────────────────────────────

    /** Returns a Flow that emits RoomState whenever the room changes in Firebase. */
    fun listenToRoom(roomCode: String): Flow<RoomState> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    close()
                    return
                }
                val room = RoomState(
                    roomCode = roomCode,
                    hostUid = snapshot.child("hostUid").getValue(String::class.java) ?: "",
                    guestUid = snapshot.child("guestUid").getValue(String::class.java) ?: "",
                    status = snapshot.child("status").getValue(String::class.java) ?: RoomStatus.WAITING.name,
                    gridSize = snapshot.child("gridSize").getValue(Int::class.java) ?: 6,
                    gameVariant = snapshot.child("gameVariant").getValue(String::class.java) ?: GameVariant.CLASSIC.name,
                    hostName = snapshot.child("hostName").getValue(String::class.java) ?: "Player 1",
                    guestName = snapshot.child("guestName").getValue(String::class.java) ?: "Player 2",
                    hostColorIndex = snapshot.child("hostColorIndex").getValue(Int::class.java) ?: 0,
                    guestColorIndex = snapshot.child("guestColorIndex").getValue(Int::class.java) ?: 1,
                    board = snapshot.child("board").children.mapNotNull { it.getValue(String::class.java) },
                    currentPlayerId = snapshot.child("currentPlayerId").getValue(Int::class.java) ?: 1,
                    moveCount = snapshot.child("moveCount").getValue(Int::class.java) ?: 0,
                    winnerId = snapshot.child("winnerId").getValue(Int::class.java) ?: 0,
                    gameStatus = snapshot.child("gameStatus").getValue(String::class.java) ?: GameStatus.IN_PROGRESS.name,
                    lastMoveRow = snapshot.child("lastMoveRow").getValue(Int::class.java) ?: -1,
                    lastMoveCol = snapshot.child("lastMoveCol").getValue(Int::class.java) ?: -1,
                    lastMoveBy = snapshot.child("lastMoveBy").getValue(Int::class.java) ?: 0,
                    lastMoveTimestamp = snapshot.child("lastMoveTimestamp").getValue(Long::class.java) ?: 0
                )
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        roomsRef.child(roomCode).addValueEventListener(listener)
        awaitClose { roomsRef.child(roomCode).removeEventListener(listener) }
    }

    // ── Leave / Cleanup ───────────────────────────────────────────

    suspend fun leaveRoom(roomCode: String) {
        val uid = currentUid()
        val snapshot = roomsRef.child(roomCode).get().await()
        if (!snapshot.exists()) return

        val hostUid = snapshot.child("hostUid").getValue(String::class.java) ?: ""
        val status = snapshot.child("status").getValue(String::class.java) ?: ""

        if (status == RoomStatus.WAITING.name && hostUid == uid) {
            // Host leaving a waiting room — delete it
            roomsRef.child(roomCode).removeValue().await()
        } else {
            // Mark game as finished
            roomsRef.child(roomCode).child("status")
                .setValue(RoomStatus.FINISHED.name).await()
        }
    }

    /** Deserialize flat board list back to 2D CellState grid. */
    fun deserializeBoard(serialized: List<String>, gridSize: Int): List<List<CellState>> {
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
