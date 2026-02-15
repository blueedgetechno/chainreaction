package com.blueedge.chainreaction.data.model

/**
 * Represents the current state of the game
 */
sealed class GameState {
    data object Setup : GameState()
    data class Playing(
        val currentPlayerId: Int,
        val moveCount: Int = 0,
        val gameStartTime: Long = System.currentTimeMillis()
    ) : GameState()
    data class GameOver(
        val winnerId: Int,
        val gameDuration: Long,
        val totalMoves: Int
    ) : GameState()
    data object Paused : GameState()
}

/**
 * Represents the game mode
 */
enum class GameMode {
    LOCAL_MULTIPLAYER,
    BOT_EASY,
    BOT_MEDIUM,
    BOT_HARD;
    
    val isBot: Boolean get() = this != LOCAL_MULTIPLAYER
    
    val difficulty: BotDifficulty? get() = when (this) {
        BOT_EASY -> BotDifficulty.EASY
        BOT_MEDIUM -> BotDifficulty.MEDIUM
        BOT_HARD -> BotDifficulty.HARD
        else -> null
    }
}

enum class BotDifficulty {
    EASY,
    MEDIUM,
    HARD
}
