package br.pucpr.dailypuzzle.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.theme.LossColor
import br.pucpr.dailypuzzle.ui.theme.TileCorrect

fun attemptsText(game: Game, attempts: Int): String = when (game) {
    Game.HASHTAG -> if (attempts == 1) "1 movimento" else "$attempts movimentos"
    Game.SUDOKU -> when (attempts) {
        0 -> "sem erros"
        1 -> "1 erro"
        else -> "$attempts erros"
    }
    else -> if (attempts == 1) "1 tentativa" else "$attempts tentativas"
}

fun winSummary(game: Game, attempts: Int): String {
    val amount = attemptsText(game, attempts)
    return when (game) {
        Game.TERMO -> "Você descobriu a palavra em $amount."
        Game.CACA_PALAVRAS -> "Você encontrou todas as palavras em $amount."
        Game.HASHTAG -> "Você reordenou as letras em $amount."
        Game.SUDOKU -> if (attempts == 0) {
            "Você completou o Sudoku sem errar."
        } else {
            "Você completou o Sudoku com $amount."
        }
        Game.CRUZADINHA_MINI -> "Você completou a cruzadinha em $amount."
    }
}

@Composable
fun GameModeBanner(
    practice: Boolean,
    isToday: Boolean,
    dateLabel: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    when {
        practice -> InfoBanner(
            icon = R.drawable.ic_replay,
            text = "Modo treino: não conta para a sequência.",
            color = accent,
            modifier = modifier
        )

        !isToday -> InfoBanner(
            icon = R.drawable.ic_calendar_today,
            text = "Desafio de $dateLabel",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = modifier
        )
    }
}

@Composable
fun PreviousResultScreen(
    game: Game,
    result: GameProgressEntity,
    dateLabel: String,
    onBack: () -> Unit,
    onPlayAgain: () -> Unit,
    onShowResult: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = game.title, onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            PreviousResultCard(
                game = game,
                result = result,
                dateLabel = dateLabel,
                onPlayAgain = onPlayAgain,
                onShowResult = onShowResult,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
fun PreviousResultCard(
    game: Game,
    result: GameProgressEntity,
    dateLabel: String,
    onPlayAgain: () -> Unit,
    onShowResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val won = result.won
    val tint = if (won) TileCorrect else LossColor

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = subtleBorder(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(
                icon = if (won) R.drawable.ic_emoji_events else R.drawable.ic_sentiment_dissatisfied,
                tint = tint,
                size = 80.dp,
                iconSize = 42.dp,
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (won) "Você venceu!" else "Não foi dessa vez",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (won) {
                    winSummary(game, result.attempts)
                } else {
                    "Você não completou este desafio."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AppIcon(icon = R.drawable.ic_replay, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Jogar novamente")
            }
            TextButton(
                onClick = onShowResult,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ver resultado completo")
            }
        }
    }
}
