package com.blueedge.chainreaction.data.local.dao

import androidx.room.*
import com.blueedge.chainreaction.data.local.entities.GameStatEntity
import com.blueedge.chainreaction.data.local.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for game statistics
 */
@Dao
interface GameDao {
    @Insert
    suspend fun insertGameStat(stat: GameStatEntity)
    
    @Query("SELECT * FROM game_stats ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentGames(limit: Int = 10): Flow<List<GameStatEntity>>
    
    @Query("SELECT COUNT(*) FROM game_stats WHERE winner = :playerName")
    suspend fun getWinCount(playerName: String): Int
    
    @Query("SELECT COUNT(*) FROM game_stats")
    suspend fun getTotalGamesPlayed(): Int
    
    @Query("SELECT * FROM game_stats WHERE gameMode = :mode ORDER BY timestamp DESC")
    fun getGamesByMode(mode: String): Flow<List<GameStatEntity>>
    
    @Query("DELETE FROM game_stats")
    suspend fun clearAllStats()
}

/**
 * DAO for settings
 */
@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity>
    
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsSync(): SettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    
    @Query("UPDATE settings SET defaultGridSize = :gridSize WHERE id = 1")
    suspend fun updateGridSize(gridSize: Int)
    
    @Query("UPDATE settings SET soundEnabled = :enabled WHERE id = 1")
    suspend fun updateSoundEnabled(enabled: Boolean)
    
    @Query("UPDATE settings SET vibrationEnabled = :enabled WHERE id = 1")
    suspend fun updateVibrationEnabled(enabled: Boolean)
}
