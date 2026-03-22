package com.blueedge.chainreaction.platform

/**
 * Callback-based bridge protocol for Firebase operations.
 * Swift implements this interface; Kotlin wraps it with coroutines in IosOnlineGameRepo.
 *
 * All async operations use onSuccess/onError callbacks.
 * Kotlin Int exports as Int32 to ObjC/Swift.
 */
interface FirebaseBridge {
    fun ensureAuth(onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun getUid(): String

    fun createRoom(
        gridSize: Int, gameVariant: String, hostName: String, hostColorIndex: Int,
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    )

    fun joinRoom(
        roomCode: String, guestName: String, guestColorIndex: Int,
        onSuccess: (Boolean) -> Unit, onError: (String) -> Unit
    )

    fun findRandomMatch(
        gridSize: Int, gameVariant: String, playerName: String, playerColorIndex: Int,
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    )

    fun sendMove(
        roomCode: String, row: Int, col: Int, playerId: Int,
        onSuccess: () -> Unit, onError: (String) -> Unit
    )

    fun syncGameState(
        roomCode: String, board: List<String>,
        currentPlayerId: Int, moveCount: Int,
        winnerId: Int, gameStatus: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    )

    fun listenToRoom(
        roomCode: String,
        onUpdate: (Map<String, Any>) -> Unit,
        onError: (String) -> Unit
    )

    fun stopListening(roomCode: String)

    fun leaveRoom(
        roomCode: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    )
}
