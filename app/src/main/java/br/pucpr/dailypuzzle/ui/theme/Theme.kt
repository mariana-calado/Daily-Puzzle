package br.pucpr.dailypuzzle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    secondary = Slate40,
    tertiary = Coral40
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    secondary = Slate80,
    tertiary = Coral80
)

// Paleta fixa (sem "dynamic color" do Android 12+): o app fica igual em qualquer aparelho
// e as duas pessoas da dupla trabalham com as mesmas cores.
@Composable
fun DailyPuzzleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
