package com.blueedge.chainreaction.di

import android.content.Context
import androidx.room.Room
import com.blueedge.chainreaction.data.local.GameDatabase
import com.blueedge.chainreaction.data.local.dao.GameDao
import com.blueedge.chainreaction.data.local.dao.SettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideGameDatabase(
        @ApplicationContext context: Context
    ): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            "chain_reaction_db"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideGameDao(database: GameDatabase): GameDao {
        return database.gameDao()
    }
    
    @Provides
    @Singleton
    fun provideSettingsDao(database: GameDatabase): SettingsDao {
        return database.settingsDao()
    }
}
