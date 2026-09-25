package br.pucpr.dailypuzzle.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.common.EmBreveScreen
import br.pucpr.dailypuzzle.ui.games.cacapalavras.CacaPalavrasScreen
import br.pucpr.dailypuzzle.ui.home.HomeScreen

@Composable
fun DailyPuzzleNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onGameClick = { game -> navController.navigate(Routes.forGame(game)) },
                onStatsClick = { navController.navigate(Routes.STATS) }
            )
        }

        composable(Routes.CACA_PALAVRAS) {
            CacaPalavrasScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    navController.navigate(Routes.result(Game.CACA_PALAVRAS)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.TERMO) {
            EmBreveScreen(
                title = Game.TERMO.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HASHTAG) {
            EmBreveScreen(
                title = Game.HASHTAG.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SUDOKU) {
            EmBreveScreen(
                title = Game.SUDOKU.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CRUZADINHA_MINI) {
            EmBreveScreen(
                title = Game.CRUZADINHA_MINI.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            EmBreveScreen(
                title = "Estatísticas",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument(Routes.ARG_GAME_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(Routes.ARG_GAME_ID)
            val game = Game.entries.firstOrNull { it.name == gameId }
            EmBreveScreen(
                title = if (game != null) "Resultado: ${game.title}" else "Resultado",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
