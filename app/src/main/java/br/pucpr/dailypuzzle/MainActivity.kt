package br.pucpr.dailypuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.pucpr.dailypuzzle.ui.home.HomeScreen
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme

/**
 * Única Activity do app. Por enquanto ela abre direto a Home;
 * no próximo passo o conteúdo vira o NavGraph, que controla a troca de telas.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyPuzzleTheme {
                HomeScreen(
                    onGameClick = { /* TODO: navegação entra no passo 3 */ },
                    onStatsClick = { /* TODO: navegação entra no passo 3 */ }
                )
            }
        }
    }
}
