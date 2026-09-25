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
import br.pucpr.dailypuzzle.ui.result.ResultScreen
import br.pucpr.dailypuzzle.ui.stats.StatsScreen

private fun dateArgument() = listOf(
    navArgument(Routes.ARG_DATE) {
        type = NavType.StringType
        defaultValue = ""
    }
)

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

        composable(
            route = Routes.pattern(Game.CACA_PALAVRAS),
            arguments = dateArgument()
        ) { entry ->
            val date = entry.arguments?.getString(Routes.ARG_DATE).orEmpty()
            CacaPalavrasScreen(
                onBack = { navController.popBackStack() },
                onShowResult = {
                    navController.navigate(Routes.result(Game.CACA_PALAVRAS, date)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.pattern(Game.TERMO),
            arguments = dateArgument()
        ) {
            EmBreveScreen(
                title = Game.TERMO.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.pattern(Game.HASHTAG),
            arguments = dateArgument()
        ) {
            EmBreveScreen(
                title = Game.HASHTAG.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.pattern(Game.SUDOKU),
            arguments = dateArgument()
        ) {
            EmBreveScreen(
                title = Game.SUDOKU.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.pattern(Game.CRUZADINHA_MINI),
            arguments = dateArgument()
        ) {
            EmBreveScreen(
                title = Game.CRUZADINHA_MINI.title,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                onPlayDay = { game, date -> navController.navigate(Routes.forGame(game, date)) }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument(Routes.ARG_GAME_ID) { type = NavType.StringType },
                navArgument(Routes.ARG_DATE) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ResultScreen(
                onBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onStats = { navController.navigate(Routes.STATS) }
            )
        }
    }
}
