package com.blueedge.chainreaction.di

import com.blueedge.chainreaction.ai.BotStrategy
import com.blueedge.chainreaction.ai.EasyBot
import com.blueedge.chainreaction.ai.HardBot
import com.blueedge.chainreaction.ai.MediumBot
import com.blueedge.chainreaction.data.model.BotDifficulty
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
    
    @Provides
    @Singleton
    fun provideEasyBot(gameEngine: GameEngine): EasyBot {
        return EasyBot(gameEngine)
    }
    
    @Provides
    @Singleton
    fun provideMediumBot(gameEngine: GameEngine): MediumBot {
        return MediumBot(gameEngine)
    }
    
    @Provides
    @Singleton
    fun provideHardBot(gameEngine: GameEngine): HardBot {
        return HardBot(gameEngine)
    }
}

/**
 * Helper to get the appropriate bot strategy
 */
fun getBotStrategy(
    difficulty: BotDifficulty,
    easyBot: EasyBot,
    mediumBot: MediumBot,
    hardBot: HardBot
): BotStrategy {
    return when (difficulty) {
        BotDifficulty.EASY -> easyBot
        BotDifficulty.MEDIUM -> mediumBot
        BotDifficulty.HARD -> hardBot
    }
}
