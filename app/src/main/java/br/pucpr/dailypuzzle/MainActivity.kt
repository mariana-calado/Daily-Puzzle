package br.pucpr.dailypuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.pucpr.dailypuzzle.ui.home.HomeScreen
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyPuzzleTheme {
                HomeScreen(
                    onGameClick = { },
                    onStatsClick = { }
                )
            }
        }
    }
}
