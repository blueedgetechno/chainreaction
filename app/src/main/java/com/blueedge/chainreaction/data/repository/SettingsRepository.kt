package com.blueedge.chainreaction.data.repository

import com.blueedge.chainreaction.data.local.dao.SettingsDao
import com.blueedge.chainreaction.data.local.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user settings
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    fun getSettings(): Flow<SettingsEntity> {
        return settingsDao.getSettings()
    }
    
    suspend fun getSettingsSync(): SettingsEntity {
        return settingsDao.getSettingsSync() ?: SettingsEntity()
    }
    
    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertSettings(settings)
    }
    
    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.updateSettings(settings)
    }
    
    suspend fun updateGridSize(gridSize: Int) {
        settingsDao.updateGridSize(gridSize)
    }
    
    suspend fun updateSoundEnabled(enabled: Boolean) {
        settingsDao.updateSoundEnabled(enabled)
    }
    
    suspend fun updateVibrationEnabled(enabled: Boolean) {
        settingsDao.updateVibrationEnabled(enabled)
    }
}
