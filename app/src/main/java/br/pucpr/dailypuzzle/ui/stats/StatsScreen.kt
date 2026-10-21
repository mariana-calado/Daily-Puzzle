package br.pucpr.dailypuzzle.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import br.pucpr.dailypuzzle.ui.common.IconBadge
import br.pucpr.dailypuzzle.ui.common.InfoBanner
import br.pucpr.dailypuzzle.ui.common.StatTile
import br.pucpr.dailypuzzle.ui.common.subtleBorder
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.FlameColor
import br.pucpr.dailypuzzle.ui.theme.LossColor
import br.pucpr.dailypuzzle.ui.theme.TileCorrect
import br.pucpr.dailypuzzle.ui.theme.gameAccent
import br.pucpr.dailypuzzle.ui.theme.gameIcon

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onPlayDay: (Game, String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsContent(
        uiState = uiState,
        onBack = onBack,
        onPlayDay = onPlayDay,
        onClearHistory = viewModel::onClearHistory
    )
}

@Composable
private fun StatsContent(
    uiState: StatsUiState,
    onBack: () -> Unit,
    onPlayDay: (Game, String) -> Unit,
    onClearHistory: (Game) -> Unit
) {
    var gameToClear by remember { mutableStateOf<Game?>(null) }

    gameToClear?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToClear = null },
            title = { Text(text = "Apagar histórico?") },
            text = {
                Text(
                    text = "Todos os resultados e estatísticas de ${game.title} serão removidos. " +
                        "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory(game)
                        gameToClear = null
                    }
                ) {
                    Text(text = "Apagar", color = LossColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToClear = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Estatísticas", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewRow(uiState = uiState) }

            item {
                InfoBanner(
                    icon = R.drawable.ic_info,
                    text = "Toque num dia vazio para jogá-lo.",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.games, key = { it.game.name }) { stats ->
                GameStatsCard(
                    stats = stats,
                    onPlayDay = { date -> onPlayDay(stats.game, date) },
                    onClear = { gameToClear = stats.game }
                )
            }
        }
    }
}

@Composable
private fun OverviewRow(uiState: StatsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatTile(
            icon = R.drawable.ic_local_fire_department,
            value = uiState.currentStreak.toString(),
            label = "Sequência atual",
            accent = FlameColor,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        StatTile(
            icon = R.drawable.ic_emoji_events,
            value = uiState.totalWon.toString(),
            label = "Vitórias",
            accent = TileCorrect,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        StatTile(
            icon = R.drawable.ic_trending_up,
            value = "${uiState.winRate}%",
            label = "Taxa de vitória",
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun GameStatsCard(
    stats: GameStatsUi,
    onPlayDay: (String) -> Unit,
    onClear: () -> Unit
) {
    val accent = gameAccent(stats.game)

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = subtleBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = gameIcon(stats.game), tint = accent, size = 42.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stats.game.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (stats.totalPlayed > 0) {
                    IconButton(onClick = onClear) {
                        AppIcon(
                            icon = R.drawable.ic_delete,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = "Apagar histórico de ${stats.game.title}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                MiniStat(stats.currentStreak.toString(), "Sequência", accent, Modifier.weight(1f))
                MiniStat(stats.bestStreak.toString(), "Recorde", accent, Modifier.weight(1f))
                MiniStat(stats.totalPlayed.toString(), "Jogos", accent, Modifier.weight(1f))
                MiniStat(stats.totalWon.toString(), "Vitórias", accent, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stats.days.forEach { day ->
                    DayCell(
                        day = day,
                        accent = accent,
                        onClick = { onPlayDay(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DayCell(
    day: DayStatus,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        day.won -> accent
        day.played -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    var cell = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(MaterialTheme.shapes.small)
        .background(background)

    if (day.isToday) {
        cell = cell.border(2.dp, accent, MaterialTheme.shapes.small)
    }
    if (!day.played) {
        cell = cell.clickable(onClick = onClick)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.weekday,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = cell,
            contentAlignment = Alignment.Center
        ) {
            when {
                day.won -> AppIcon(R.drawable.ic_check, tint = Color.White, size = 20.dp)
                day.played -> AppIcon(R.drawable.ic_close, tint = Color.White, size = 20.dp)
                else -> Text(
                    text = day.dayNumber,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsContentPreview() {
    val weekdays = listOf("Q", "Q", "S", "S", "D", "S", "T")
    val days = weekdays.mapIndexed { index, letter ->
        DayStatus(
            date = "2026-10-${9 + index}",
            weekday = letter,
            dayNumber = (9 + index).toString(),
            played = index in 2..5,
            won = index in 2..4,
            isToday = index == 6
        )
    }

    DailyPuzzleTheme {
        StatsContent(
            uiState = StatsUiState(
                games = Game.entries.map { game ->
                    GameStatsUi(
                        game = game,
                        currentStreak = 3,
                        bestStreak = 5,
                        totalPlayed = 8,
                        totalWon = 7,
                        days = days
                    )
                },
                currentStreak = 3,
                bestStreak = 5,
                totalPlayed = 40,
                totalWon = 35
            ),
            onBack = {},
            onPlayDay = { _, _ -> },
            onClearHistory = {}
        )
    }
}
