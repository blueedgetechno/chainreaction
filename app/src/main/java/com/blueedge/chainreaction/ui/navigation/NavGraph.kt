package com.blueedge.chainreaction.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.blueedge.chainreaction.ui.screens.HowToPlayScreen
import com.blueedge.chainreaction.ui.screens.MainMenuScreen
import com.blueedge.chainreaction.ui.screens.SettingsScreen

object Routes {
    const val MAIN_MENU = "main_menu"
    const val GAME_SETUP = "game_setup/{mode}"
    const val GAME = "game"
    const val GAME_END = "game_end/{winnerId}/{capturedCells}/{moves}/{duration}"
    const val SETTINGS = "settings"
    const val HOW_TO_PLAY = "how_to_play"
    const val IN_GAME_SETTINGS = "in_game_settings"
    const val IN_GAME_HOW_TO_PLAY = "in_game_how_to_play"
    const val SETUP_HOW_TO_PLAY = "setup_how_to_play"

    fun gameSetup(mode: String) = "game_setup/$mode"
    fun gameEnd(winnerId: Int, capturedCells: Int, moves: Int, duration: Long) =
        "game_end/$winnerId/$capturedCells/$moves/$duration"
}

@Composable
fun ChainReactionNavGraph(navController: NavHostController) {
    // Deeper navigation: current page fades out with scale up (100% → 105%), target fades in with scale up (95% → 100%)
    val enterTransition = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))
    val exitTransition = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300))
    
    // Popping back: opposite scaling
    val popEnterTransition = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300))
    val popExitTransition = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f, animationSpec = tween(300))
    
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_MENU
    ) {
        composable(
            Routes.MAIN_MENU,
            enterTransition = { popEnterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            MainMenuScreen(
                onLocalMultiplayer = {
                    navController.navigate(Routes.gameSetup("local"))
                },
                onPlayVsBot = {
                    navController.navigate(Routes.gameSetup("bot"))
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onHowToPlay = {
                    navController.navigate(Routes.HOW_TO_PLAY)
                }
            )
        }

        composable(
            Routes.SETTINGS,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.HOW_TO_PLAY,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.GAME_SETUP,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "local"
            GameSetupScreen(
                gameMode = if (mode == "bot") GameMode.VS_BOT else GameMode.LOCAL_MULTIPLAYER,
                onStartGame = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onBack = { navController.popBackStack() },
                onHowToPlay = {
                    navController.navigate(Routes.SETUP_HOW_TO_PLAY)
                }
            )
        }

        composable(
            Routes.SETUP_HOW_TO_PLAY,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.GAME,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            GameBoardScreen(
                onGameEnd = { winnerId, capturedCells, moves, duration ->
                    navController.navigate(
                        Routes.gameEnd(winnerId, capturedCells, moves, duration)
                    ) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onPlayAgain = {
                    navController.popBackStack(Routes.GAME, inclusive = true)
                    navController.navigate(Routes.GAME)
                },
                onExit = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                },
                onOpenSettings = {
                    navController.navigate(Routes.IN_GAME_SETTINGS)
                }
            )
        }

        composable(
            Routes.IN_GAME_SETTINGS,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                isInGame = true,
                onHowToPlay = {
                    navController.navigate(Routes.IN_GAME_HOW_TO_PLAY)
                },
                onRestart = {
                    navController.popBackStack(Routes.GAME, inclusive = true)
                    navController.navigate(Routes.GAME)
                },
                onExitToMenu = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                }
            )
        }

        composable(
            Routes.IN_GAME_HOW_TO_PLAY,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.GAME_END,
            arguments = listOf(
                navArgument("winnerId") { type = NavType.IntType },
                navArgument("capturedCells") { type = NavType.IntType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType }
            ),
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            GameEndScreen(
                winnerId = args.getInt("winnerId"),
                capturedCells = args.getInt("capturedCells"),
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
