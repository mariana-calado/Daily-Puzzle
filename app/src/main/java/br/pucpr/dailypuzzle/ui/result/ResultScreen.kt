package br.pucpr.dailypuzzle.ui.result

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.common.AppIcon
import br.pucpr.dailypuzzle.ui.common.AppTopBar
import br.pucpr.dailypuzzle.ui.common.StatTile
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.FlameColor
import br.pucpr.dailypuzzle.ui.theme.LossColor
import br.pucpr.dailypuzzle.ui.theme.TileCorrect
import br.pucpr.dailypuzzle.ui.theme.gameAccent

@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onStats: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ResultContent(
        uiState = uiState,
        onBack = onBack,
        onHome = onHome,
        onStats = onStats
    )
}

@Composable
private fun ResultContent(
    uiState: ResultUiState,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onStats: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Resultado", onBack = onBack) }
    ) { innerPadding ->
        if (uiState.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutcomeHero(uiState = uiState)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Seu desempenho em ${uiState.game.title}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    icon = R.drawable.ic_local_fire_department,
                    value = uiState.currentStreak.toString(),
                    label = "Sequência",
                    accent = FlameColor,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = R.drawable.ic_military_tech,
                    value = uiState.bestStreak.toString(),
                    label = "Recorde",
                    accent = gameAccent(uiState.game),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    icon = R.drawable.ic_sports_esports,
                    value = uiState.totalPlayed.toString(),
                    label = "Jogos",
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = R.drawable.ic_emoji_events,
                    value = uiState.totalWon.toString(),
                    label = "Vitórias",
                    accent = TileCorrect,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AppIcon(icon = R.drawable.ic_home, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Voltar ao início")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AppIcon(icon = R.drawable.ic_bar_chart, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ver estatísticas")
            }
        }
    }
}

@Composable
private fun OutcomeHero(uiState: ResultUiState) {
    val brush = when {
        !uiState.played -> Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.secondary)
        )
        uiState.won -> Brush.linearGradient(listOf(TileCorrect, Color(0xFF2B7A74)))
        else -> Brush.linearGradient(listOf(LossColor, Color(0xFF8E3B5E)))
    }

    val icon = when {
        !uiState.played -> R.drawable.ic_schedule
        uiState.won -> R.drawable.ic_emoji_events
        else -> R.drawable.ic_sentiment_dissatisfied
    }

    val title = when {
        !uiState.played -> "Ainda não jogado"
        uiState.won -> "Você venceu!"
        else -> "Não foi dessa vez"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(brush)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(icon = icon, tint = Color.White, size = 46.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = uiState.game.title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroChip(icon = R.drawable.ic_calendar_today, text = uiState.dateLabel)
        }

        if (uiState.played) {
            Spacer(modifier = Modifier.height(8.dp))
            HeroChip(
                icon = R.drawable.ic_replay,
                text = "${uiState.attempts} tentativas"
            )
        }
    }
}

@Composable
private fun HeroChip(@DrawableRes icon: Int, text: String) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon = icon, tint = Color.White, size = 16.dp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentPreview() {
    DailyPuzzleTheme {
        ResultContent(
            uiState = ResultUiState(
                loading = false,
                game = Game.CACA_PALAVRAS,
                dateLabel = "Quinta-feira, 15 de outubro",
                played = true,
                won = true,
                attempts = 6,
                currentStreak = 3,
                bestStreak = 5,
                totalPlayed = 8,
                totalWon = 7
            ),
            onBack = {},
            onHome = {},
            onStats = {}
        )
    }
}
