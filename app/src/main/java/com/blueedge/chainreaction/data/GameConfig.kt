package com.blueedge.chainreaction.data

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
    var gridSize: Int = 6
    var numPlayers: Int = 2
    var player1Name: String = "Player 1"
    var player1ColorIndex: Int = 0
    var player2Name: String = "Player 2"
    var player2ColorIndex: Int = 1
    var botDifficulty: BotDifficulty = BotDifficulty.MEDIUM
    var soundEnabled by mutableStateOf(false)
    var vibrationEnabled by mutableStateOf(true)
    var musicEnabled by mutableStateOf(true)
    var appFont by mutableStateOf(AppFont.COMIC_RELIEF)
    var language by mutableStateOf("English")

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
