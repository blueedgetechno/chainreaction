package com.blueedge.chainreaction.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.blueedge.chainreaction.data.GameMode
import com.blueedge.chainreaction.ui.screens.GameBoardScreen
import com.blueedge.chainreaction.ui.screens.GameEndScreen
import com.blueedge.chainreaction.ui.screens.GameSetupScreen
import com.blueedge.chainreaction.ui.screens.MainMenuScreen
import com.blueedge.chainreaction.ui.screens.SettingsScreen

object Routes {
    const val MAIN_MENU = "main_menu"
    const val GAME_SETUP = "game_setup/{mode}"
    const val GAME = "game"
    const val GAME_END = "game_end/{winnerId}/{p1Score}/{p2Score}/{moves}/{duration}"
    const val SETTINGS = "settings"

    fun gameSetup(mode: String) = "game_setup/$mode"
    fun gameEnd(winnerId: Int, p1Score: Int, p2Score: Int, moves: Int, duration: Long) =
        "game_end/$winnerId/$p1Score/$p2Score/$moves/$duration"
}

@Composable
fun ChainReactionNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_MENU
    ) {
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                onLocalMultiplayer = {
                    navController.navigate(Routes.gameSetup("local"))
                },
                onPlayVsBot = {
                    navController.navigate(Routes.gameSetup("bot"))
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.GAME_SETUP,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "local"
            GameSetupScreen(
                gameMode = if (mode == "bot") GameMode.VS_BOT else GameMode.LOCAL_MULTIPLAYER,
                onStartGame = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAME) {
            GameBoardScreen(
                onGameEnd = { winnerId, p1Score, p2Score, moves, duration ->
                    navController.navigate(
                        Routes.gameEnd(winnerId, p1Score, p2Score, moves, duration)
                    ) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onExit = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                }
            )
        }

        composable(
            route = Routes.GAME_END,
            arguments = listOf(
                navArgument("winnerId") { type = NavType.IntType },
                navArgument("p1Score") { type = NavType.IntType },
                navArgument("p2Score") { type = NavType.IntType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            GameEndScreen(
                winnerId = args.getInt("winnerId"),
                player1Score = args.getInt("p1Score"),
                player2Score = args.getInt("p2Score"),
                totalMoves = args.getInt("moves"),
                durationSeconds = args.getLong("duration"),
                onPlayAgain = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onMainMenu = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                }
            )
        }
    }
}
