package com.blueedge.chainreaction.di

import com.blueedge.chainreaction.domain.GameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for game-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object GameModule {
    
    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine {
        return GameEngine()
    }
}
