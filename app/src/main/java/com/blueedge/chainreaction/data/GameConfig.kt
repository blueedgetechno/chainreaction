package com.blueedge.chainreaction.data

object GameConfig {
    var gameMode: GameMode = GameMode.LOCAL_MULTIPLAYER
    var gridSize: Int = 6
    var player1Name: String = "Player 1"
    var player1ColorIndex: Int = 0
    var player2Name: String = "Player 2"
    var player2ColorIndex: Int = 1
    var botDifficulty: BotDifficulty = BotDifficulty.MEDIUM
    var soundEnabled: Boolean = true
    var vibrationEnabled: Boolean = true
}
