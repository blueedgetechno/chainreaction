package com.blueedge.chainreaction.presentation.navigation

import com.blueedge.chainreaction.data.model.GameMode

/**
 * Navigation destinations for the app
 */
sealed class Screen(val route: String) {
    data object MainMenu : Screen("main_menu")
    data object GameSetup : Screen("game_setup/{gameMode}") {
        fun createRoute(gameMode: GameMode) = "game_setup/${gameMode.name}"
    }
    data object Game : Screen("game/{gridSize}/{gameMode}") {
        fun createRoute(gridSize: Int, gameMode: GameMode) = "game/$gridSize/${gameMode.name}"
    }
    data object GameEnd : Screen("game_end/{winnerId}/{moves}/{duration}") {
        fun createRoute(winnerId: Int, moves: Int, duration: Long) = "game_end/$winnerId/$moves/$duration"
    }
    data object Settings : Screen("settings")
}
