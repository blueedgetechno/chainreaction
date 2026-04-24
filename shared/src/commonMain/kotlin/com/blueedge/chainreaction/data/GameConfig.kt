package com.blueedge.chainreaction.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.blueedge.chainreaction.platform.InMemoryStorage
import com.blueedge.chainreaction.platform.PlatformStorage

enum class AppFont(val displayName: String) {
    DEFAULT("Default"),
    DYNAPUFF("DynaPuff"),
    SOUR_GUMMY("Sour Gummy"),
    COMIC_RELIEF("Comic Relief")
}

object GameConfig {
    var gameMode: GameMode = GameMode.LOCAL_MULTIPLAYER
    var gameVariant: GameVariant = GameVariant.CLASSIC
    var gridSize: Int = 6
    var numPlayers: Int = 2
    var player1Name: String = "Player 1"
    var player1ColorIndex: Int = 0
    var player2Name: String = "Player 2"
    var player2ColorIndex: Int = 1
    var botDifficulty: BotDifficulty = BotDifficulty.MEDIUM
    var soundEnabled by mutableStateOf(true)
    var vibrationEnabled by mutableStateOf(false)
    var musicEnabled by mutableStateOf(true)
    var appFont by mutableStateOf(AppFont.COMIC_RELIEF)
    var language by mutableStateOf("English")

    /** Platform storage — set during app startup via [initStorage]. */
    var storage: PlatformStorage = InMemoryStorage()
        private set

    fun initStorage(platformStorage: PlatformStorage) {
        storage = platformStorage
    }

    fun load() {
        val s = storage
        gameMode = try {
            GameMode.valueOf(s.getString("gameMode", gameMode.name))
        } catch (_: Exception) { gameMode }
        gameVariant = try {
            GameVariant.valueOf(s.getString("gameVariant", gameVariant.name))
        } catch (_: Exception) { gameVariant }
        gridSize = s.getInt("gridSize", gridSize)
        numPlayers = s.getInt("numPlayers", numPlayers)
        botDifficulty = try {
            BotDifficulty.valueOf(s.getString("botDifficulty", botDifficulty.name))
        } catch (_: Exception) { botDifficulty }
        soundEnabled = s.getBoolean("soundEnabled", soundEnabled)
        vibrationEnabled = s.getBoolean("vibrationEnabled", vibrationEnabled)
        musicEnabled = s.getBoolean("musicEnabled", musicEnabled)
        appFont = try {
            AppFont.valueOf(s.getString("appFont", appFont.name))
        } catch (_: Exception) { appFont }
        language = s.getString("language", language)
    }

    fun save() {
        val s = storage
        s.putString("gameMode", gameMode.name)
        s.putString("gameVariant", gameVariant.name)
        s.putInt("gridSize", gridSize)
        s.putInt("numPlayers", numPlayers)
        s.putString("botDifficulty", botDifficulty.name)
        s.putBoolean("soundEnabled", soundEnabled)
        s.putBoolean("vibrationEnabled", vibrationEnabled)
        s.putBoolean("musicEnabled", musicEnabled)
        s.putString("appFont", appFont.name)
        s.putString("language", language)
        s.apply()
    }

    fun getPlayers(): List<PlayerInfo> {
        return when (gameMode) {
            GameMode.VS_BOT -> listOf(
                PlayerInfo(1, player1Name, player1ColorIndex),
                PlayerInfo(2, player2Name, player2ColorIndex, isBot = true)
            )
            else -> (1..numPlayers).map { i ->
                PlayerInfo(i, "Player $i", i - 1)
            }
        }
    }
}
