package br.pucpr.dailypuzzle.ui.result

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultContent(
    uiState: ResultUiState,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onStats: () -> Unit
) {
    val accent = gameAccent(uiState.game)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resultado",
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        !uiState.played -> "🕓"
                        uiState.won -> "🎉"
                        else -> "😕"
                    },
                    fontSize = 44.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    !uiState.played -> "Desafio ainda não jogado"
                    uiState.won -> "Você venceu!"
                    else -> "Não foi dessa vez"
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${uiState.game.title} · ${uiState.dateLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (uiState.played) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${uiState.attempts} tentativas",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Sequência",
                    value = uiState.currentStreak.toString(),
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Recorde",
                    value = uiState.bestStreak.toString(),
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Jogos",
                    value = uiState.totalPlayed.toString(),
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Vitórias",
                    value = uiState.totalWon.toString(),
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Voltar ao início")
            }

            TextButton(
                onClick = onStats,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ver estatísticas")
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                dateLabel = "Sexta-feira, 25 de setembro",
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
