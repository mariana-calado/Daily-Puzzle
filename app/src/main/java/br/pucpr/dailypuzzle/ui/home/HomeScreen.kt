package br.pucpr.dailypuzzle.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme

/**
 * Tela inicial. Ela não conhece navegação: recebe funções (onGameClick, onStatsClick)
 * e quem decide para onde ir é o NavGraph. Isso deixa a tela fácil de testar e de visualizar
 * no Preview.
 */
@Composable
fun HomeScreen(
    onGameClick: (Game) -> Unit,
    onStatsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    // Lê o StateFlow do ViewModel. "by" faz uiState ser o valor atual, e a tela
    // se redesenha sozinha quando esse valor muda.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onGameClick = onGameClick,
        onStatsClick = onStatsClick
    )
}

/**
 * A parte visual pura: recebe o estado pronto e só desenha. Como não depende do ViewModel,
 * dá para mostrar no Preview do Android Studio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onGameClick: (Game) -> Unit,
    onStatsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desafios do dia") },
                actions = {
                    TextButton(onClick = onStatsClick) { Text("Estatísticas") }
                }
            )
        }
    ) { innerPadding ->
        // Grid de 2 colunas. "Lazy" = só monta os cards visíveis na tela.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(uiState.games, key = { game -> game.name }) { game ->
                GameCard(game = game, onClick = { onGameClick(game) })
            }
        }
    }
}

@Composable
private fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(170.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = emojiFor(game), fontSize = 30.sp)
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Ícone de cada card. Fica aqui (e não no enum Game) para o arquivo de contrato
// da Fase 0 não precisar mudar quando a Home mudar de visual.
private fun emojiFor(game: Game): String = when (game) {
    Game.TERMO -> "🟩"
    Game.CACA_PALAVRAS -> "🔎"
    Game.HASHTAG -> "🔤"
    Game.SUDOKU -> "🔢"
    Game.CRUZADINHA_MINI -> "✏️"
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    DailyPuzzleTheme {
        HomeContent(
            uiState = HomeUiState(),
            onGameClick = {},
            onStatsClick = {}
        )
    }
}
