package com.blueedge.chainreaction.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing game statistics
 */
@Entity(tableName = "game_stats")
data class GameStatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String,  // "LOCAL" or "BOT"
    val winner: String,
    val loser: String,
    val gridSize: Int,
    val totalMoves: Int,
    val durationSeconds: Long,
    val difficulty: String?,  // For bot games: "EASY", "MEDIUM", or "HARD"
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Entity for storing user settings
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val defaultGridSize: Int = 6,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showTurnTransition: Boolean = true,
    val turnTimerSeconds: Int = 30,
    val player1Name: String = "Player 1",
    val player2Name: String = "Player 2",
    val player1ColorHex: Long = 0xFF2196F3,  // Material Blue
    val player2ColorHex: Long = 0xFFF44336   // Material Red
)
