package com.blueedge.chainreaction.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppFont(val displayName: String) {
    DEFAULT("Default"),
    DYNAPUFF("DynaPuff"),
    SOUR_GUMMY("Sour Gummy"),
    COMIC_RELIEF("Comic Relief")
}

object GameConfig {
    var gameMode: GameMode = GameMode.LOCAL_MULTIPLAYER
    var gameVariant: GameVariant = GameVariant.SIMPLE
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

    private const val PREFS_NAME = "chain_reaction_config"

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gameMode = try {
            GameMode.valueOf(prefs.getString("gameMode", gameMode.name)!!)
        } catch (_: Exception) { gameMode }
        gameVariant = try {
            GameVariant.valueOf(prefs.getString("gameVariant", gameVariant.name)!!)
        } catch (_: Exception) { gameVariant }
        gridSize = prefs.getInt("gridSize", gridSize)
        numPlayers = prefs.getInt("numPlayers", numPlayers)
        botDifficulty = try {
            BotDifficulty.valueOf(prefs.getString("botDifficulty", botDifficulty.name)!!)
        } catch (_: Exception) { botDifficulty }
        soundEnabled = prefs.getBoolean("soundEnabled", soundEnabled)
        vibrationEnabled = prefs.getBoolean("vibrationEnabled", vibrationEnabled)
        musicEnabled = prefs.getBoolean("musicEnabled", musicEnabled)
        appFont = try {
            AppFont.valueOf(prefs.getString("appFont", appFont.name)!!)
        } catch (_: Exception) { appFont }
        language = prefs.getString("language", language) ?: language
    }

    fun save(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("gameMode", gameMode.name)
            putString("gameVariant", gameVariant.name)
            putInt("gridSize", gridSize)
            putInt("numPlayers", numPlayers)
            putString("botDifficulty", botDifficulty.name)
            putBoolean("soundEnabled", soundEnabled)
            putBoolean("vibrationEnabled", vibrationEnabled)
            putBoolean("musicEnabled", musicEnabled)
            putString("appFont", appFont.name)
            putString("language", language)
            apply()
        }
    }

    fun getPlayers(): List<PlayerInfo> {
        return if (gameMode == GameMode.VS_BOT) {
            listOf(
                PlayerInfo(1, player1Name, player1ColorIndex),
                PlayerInfo(2, player2Name, player2ColorIndex, isBot = true)
            )
        } else {
            (1..numPlayers).map { i ->
                PlayerInfo(i, "Player $i", i - 1)
            }
        }
    }
}
