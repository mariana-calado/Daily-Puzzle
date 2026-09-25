package br.pucpr.dailypuzzle.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.TileAbsent
import br.pucpr.dailypuzzle.ui.theme.gameAccent
import br.pucpr.dailypuzzle.ui.theme.gameEmoji

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
        onPlayDay = onPlayDay
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    uiState: StatsUiState,
    onBack: () -> Unit,
    onPlayDay: (Game, String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estatísticas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Toque num dia vazio para jogar o desafio daquele dia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(uiState.games, key = { it.game.name }) { stats ->
                GameStatsCard(
                    stats = stats,
                    onPlayDay = { date -> onPlayDay(stats.game, date) }
                )
            }
        }
    }
}

@Composable
private fun GameStatsCard(
    stats: GameStatsUi,
    onPlayDay: (String) -> Unit
) {
    val accent = gameAccent(stats.game)

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = gameEmoji(stats.game), fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stats.game.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatNumber("Sequência", stats.currentStreak, accent, Modifier.weight(1f))
                StatNumber("Recorde", stats.bestStreak, accent, Modifier.weight(1f))
                StatNumber("Jogos", stats.totalPlayed, accent, Modifier.weight(1f))
                StatNumber("Vitórias", stats.totalWon, accent, Modifier.weight(1f))
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
private fun StatNumber(
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        day.played -> TileAbsent
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = when {
        day.played -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    var cell = modifier
        .aspectRatio(1f)
        .clip(MaterialTheme.shapes.small)
        .background(background)

    if (day.isToday) {
        cell = cell.border(
            width = 2.dp,
            color = accent,
            shape = MaterialTheme.shapes.small
        )
    }
    if (!day.played) {
        cell = cell.clickable(onClick = onClick)
    }

    Box(
        modifier = cell,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.letter,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsContentPreview() {
    val days = listOf("S", "T", "Q", "Q", "S", "S", "D").mapIndexed { index, letter ->
        DayStatus(
            date = "2026-09-${19 + index}",
            letter = letter,
            played = index < 4,
            won = index < 3,
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
                }
            ),
            onBack = {},
            onPlayDay = { _, _ -> }
        )
    }
}
