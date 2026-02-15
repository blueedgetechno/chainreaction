package com.blueedge.chainreaction.data.repository

import com.blueedge.chainreaction.data.local.dao.GameDao
import com.blueedge.chainreaction.data.local.entities.GameStatEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for game statistics
 */
@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    fun getRecentGames(limit: Int = 10): Flow<List<GameStatEntity>> {
        return gameDao.getRecentGames(limit)
    }
    
    suspend fun saveGameStat(stat: GameStatEntity) {
        gameDao.insertGameStat(stat)
    }
    
    suspend fun getWinCount(playerName: String): Int {
        return gameDao.getWinCount(playerName)
    }
    
    suspend fun getTotalGamesPlayed(): Int {
        return gameDao.getTotalGamesPlayed()
    }
    
    fun getGamesByMode(mode: String): Flow<List<GameStatEntity>> {
        return gameDao.getGamesByMode(mode)
    }
    
    suspend fun clearAllStats() {
        gameDao.clearAllStats()
    }
}
