package com.blueedge.chainreaction.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blueedge.chainreaction.data.local.dao.GameDao
import com.blueedge.chainreaction.data.local.dao.SettingsDao
import com.blueedge.chainreaction.data.local.entities.GameStatEntity
import com.blueedge.chainreaction.data.local.entities.SettingsEntity

/**
 * Room database for Chain Reaction game
 */
@Database(
    entities = [GameStatEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun settingsDao(): SettingsDao
}
