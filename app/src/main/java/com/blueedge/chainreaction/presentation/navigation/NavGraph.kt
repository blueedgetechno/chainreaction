package com.blueedge.chainreaction.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.blueedge.chainreaction.data.model.GameMode
import com.blueedge.chainreaction.presentation.end.GameEndScreen
import com.blueedge.chainreaction.presentation.game.GameBoardScreen
import com.blueedge.chainreaction.presentation.main.MainMenuScreen
import com.blueedge.chainreaction.presentation.setup.GameSetupScreen

/**
 * Navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToSetup = { gameMode ->
                    navController.navigate(Screen.GameSetup.createRoute(gameMode))
                }
            )
        }
        
        composable(
            route = Screen.GameSetup.route,
            arguments = listOf(
                navArgument("gameMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameModeString = backStackEntry.arguments?.getString("gameMode")
            val gameMode = remember(gameModeString) {
                GameMode.valueOf(gameModeString ?: GameMode.LOCAL_MULTIPLAYER.name)
            }
            
            GameSetupScreen(
                gameMode = gameMode,
                onNavigateToGame = { gridSize, finalMode ->
                    navController.navigate(Screen.Game.createRoute(gridSize, finalMode))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gridSize") { type = NavType.IntType },
                navArgument("gameMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gridSize = backStackEntry.arguments?.getInt("gridSize") ?: 6
            val gameModeString = backStackEntry.arguments?.getString("gameMode")
            val gameMode = remember(gameModeString) {
                GameMode.valueOf(gameModeString ?: GameMode.LOCAL_MULTIPLAYER.name)
            }
            
            GameBoardScreen(
                gridSize = gridSize,
                gameMode = gameMode,
                onNavigateToEnd = { winnerId, moves, duration ->
                    navController.navigate(Screen.GameEnd.createRoute(winnerId, moves, duration)) {
                        popUpTo(Screen.MainMenu.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.GameEnd.route,
            arguments = listOf(
                navArgument("winnerId") { type = NavType.IntType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val winnerId = backStackEntry.arguments?.getInt("winnerId") ?: 1
            val moves = backStackEntry.arguments?.getInt("moves") ?: 0
            val duration = backStackEntry.arguments?.getLong("duration") ?: 0
            
            GameEndScreen(
                winnerId = winnerId,
                moves = moves,
                duration = duration,
                onPlayAgain = {
                    navController.popBackStack(Screen.MainMenu.route, inclusive = false)
                },
                onMainMenu = {
                    navController.popBackStack(Screen.MainMenu.route, inclusive = false)
                }
            )
        }
    }
}
