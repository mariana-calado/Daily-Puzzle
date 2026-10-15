package br.pucpr.dailypuzzle.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.common.AppIcon
import br.pucpr.dailypuzzle.ui.common.IconBadge
import br.pucpr.dailypuzzle.ui.common.StatusPill
import br.pucpr.dailypuzzle.ui.common.subtleBorder
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.FlameColor
import br.pucpr.dailypuzzle.ui.theme.HeroEnd
import br.pucpr.dailypuzzle.ui.theme.HeroStart
import br.pucpr.dailypuzzle.ui.theme.LossColor
import br.pucpr.dailypuzzle.ui.theme.TileCorrect
import br.pucpr.dailypuzzle.ui.theme.gameAccent
import br.pucpr.dailypuzzle.ui.theme.gameIcon

@Composable
fun HomeScreen(
    onGameClick: (Game) -> Unit,
    onStatsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onGameClick = onGameClick,
        onStatsClick = onStatsClick
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onGameClick: (Game) -> Unit,
    onStatsClick: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeTopRow(dateLabel = uiState.dateLabel, streak = uiState.streak)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ProgressHero(
                    completed = uiState.completedCount,
                    total = uiState.games.size
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Jogos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }

            items(uiState.games, key = { game -> game.name }) { game ->
                GameCard(
                    game = game,
                    result = uiState.results[game],
                    onClick = { onGameClick(game) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                StatsEntry(onClick = onStatsClick)
            }
        }
    }
}

@Composable
private fun HomeTopRow(dateLabel: String, streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(HeroStart, HeroEnd))),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(icon = R.drawable.ic_extension, tint = Color.White, size = 26.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Daily Puzzle",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StreakChip(streak = streak)
    }
}

@Composable
private fun StreakChip(streak: Int) {
    val active = streak > 0
    val color = if (active) FlameColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon = R.drawable.ic_local_fire_department, tint = color, size = 20.dp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = streak.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun ProgressHero(completed: Int, total: Int) {
    val fraction = if (total == 0) 0f else completed / total.toFloat()
    val allDone = total > 0 && completed == total

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
            .padding(22.dp)
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-70).dp)
                    .requiredSize(180.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-50).dp, y = 60.dp)
                    .requiredSize(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HOJE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (allDone) "Tudo resolvido!" else "Desafios do dia",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (allDone) {
                        "Volte amanhã para novos desafios."
                    } else {
                        "$completed de $total concluídos"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.size(76.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 7.dp
                )
                Text(
                    text = "$completed/$total",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    result: Boolean?,
    onClick: () -> Unit
) {
    val accent = gameAccent(game)

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = subtleBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.height(206.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppIcon(
                icon = gameIcon(game),
                tint = accent.copy(alpha = 0.07f),
                size = 120.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 28.dp, y = 28.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                IconBadge(icon = gameIcon(game), tint = accent, size = 48.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                GameStatus(accent = accent, result = result)
            }
        }
    }
}

@Composable
private fun GameStatus(accent: Color, result: Boolean?) {
    when (result) {
        true -> StatusPill(icon = R.drawable.ic_check_circle, text = "Venceu", color = TileCorrect)
        false -> StatusPill(icon = R.drawable.ic_cancel, text = "Perdeu", color = LossColor)
        null -> StatusPill(icon = R.drawable.ic_arrow_forward, text = "Jogar", color = accent)
    }
}

@Composable
private fun StatsEntry(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = subtleBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = R.drawable.ic_bar_chart,
                tint = MaterialTheme.colorScheme.primary,
                size = 48.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estatísticas",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Sequências, vitórias e histórico",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AppIcon(
                icon = R.drawable.ic_arrow_forward,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 22.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    DailyPuzzleTheme {
        HomeContent(
            uiState = HomeUiState(
                dateLabel = "Quinta-feira, 15 de outubro",
                results = mapOf(Game.CACA_PALAVRAS to true, Game.TERMO to false),
                streak = 3
            ),
            onGameClick = {},
            onStatsClick = {}
        )
    }
}
