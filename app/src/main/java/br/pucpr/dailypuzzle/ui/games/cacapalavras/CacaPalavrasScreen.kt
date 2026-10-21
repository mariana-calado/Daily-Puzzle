package br.pucpr.dailypuzzle.ui.games.cacapalavras

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.data.content.WordSearchGenerator
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.WordCell
import br.pucpr.dailypuzzle.model.WordSearchPuzzle
import br.pucpr.dailypuzzle.ui.common.AppIcon
import br.pucpr.dailypuzzle.ui.common.AppTopBar
import br.pucpr.dailypuzzle.ui.common.IconBadge
import br.pucpr.dailypuzzle.ui.common.InfoBanner
import br.pucpr.dailypuzzle.ui.common.PreviousResultCard
import br.pucpr.dailypuzzle.ui.common.subtleBorder
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.OnTile
import br.pucpr.dailypuzzle.ui.theme.TileCorrect
import br.pucpr.dailypuzzle.ui.theme.gameAccent
import br.pucpr.dailypuzzle.ui.theme.gameIcon
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

@Composable
fun CacaPalavrasScreen(
    onBack: () -> Unit,
    onShowResult: () -> Unit,
    viewModel: CacaPalavrasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CacaPalavrasContent(
        uiState = uiState,
        onBack = onBack,
        onShowResult = onShowResult,
        onPlayAgain = viewModel::onPlayAgain,
        onSelectionChanged = viewModel::onSelectionChanged,
        onSelectionFinished = viewModel::onSelectionFinished
    )
}

@Composable
private fun CacaPalavrasContent(
    uiState: CacaPalavrasUiState,
    onBack: () -> Unit,
    onShowResult: () -> Unit,
    onPlayAgain: () -> Unit,
    onSelectionChanged: (List<WordCell>) -> Unit,
    onSelectionFinished: () -> Unit
) {
    val accent = gameAccent(Game.CACA_PALAVRAS)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = Game.CACA_PALAVRAS.title, onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.loading || uiState.puzzle == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.previousResult != null && !uiState.practice -> {
                    PreviousResultCard(
                        game = Game.CACA_PALAVRAS,
                        result = uiState.previousResult,
                        dateLabel = uiState.dateLabel,
                        onPlayAgain = onPlayAgain,
                        onShowResult = onShowResult,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp)
                    )
                }

                else -> {
                    GameBoard(
                        puzzle = uiState.puzzle,
                        uiState = uiState,
                        accent = accent,
                        onBack = onBack,
                        onShowResult = onShowResult,
                        onSelectionChanged = onSelectionChanged,
                        onSelectionFinished = onSelectionFinished
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBoard(
    puzzle: WordSearchPuzzle,
    uiState: CacaPalavrasUiState,
    accent: Color,
    onBack: () -> Unit,
    onShowResult: () -> Unit,
    onSelectionChanged: (List<WordCell>) -> Unit,
    onSelectionFinished: () -> Unit
) {
    val found = uiState.foundWords.size
    val total = puzzle.words.size
    val fraction = if (total == 0) 0f else found / total.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (uiState.practice) {
            InfoBanner(
                icon = R.drawable.ic_replay,
                text = "Modo treino: não conta para a sequência.",
                color = accent
            )
            Spacer(modifier = Modifier.height(12.dp))
        } else if (!uiState.isToday) {
            InfoBanner(
                icon = R.drawable.ic_calendar_today,
                text = "Desafio de ${uiState.dateLabel}",
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = gameIcon(Game.CACA_PALAVRAS), tint = accent, size = 46.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TEMA DO DIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = puzzle.theme,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$found/$total",
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(CircleShape)
                    .background(accent)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        WordGrid(
            puzzle = puzzle,
            foundCells = uiState.foundCells,
            selection = uiState.selection,
            accent = accent,
            enabled = !uiState.finished,
            onSelectionChanged = onSelectionChanged,
            onSelectionFinished = onSelectionFinished
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Palavras para encontrar",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        puzzle.words.map { it.word }.chunked(2).forEach { rowWords ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEach { word ->
                    WordChip(
                        word = word,
                        found = word in uiState.foundWords,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowWords.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (uiState.finished) {
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.practice) {
                MessageCard(
                    icon = R.drawable.ic_check_circle,
                    tint = accent,
                    title = "Treino concluído!",
                    message = "Este resultado não altera sua sequência.",
                    buttonLabel = "Voltar ao início",
                    buttonIcon = R.drawable.ic_home,
                    onButtonClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                MessageCard(
                    icon = R.drawable.ic_celebration,
                    tint = TileCorrect,
                    title = "Parabéns!",
                    message = "Você encontrou todas as palavras em ${uiState.attempts} tentativas.",
                    buttonLabel = "Ver resultado",
                    buttonIcon = R.drawable.ic_emoji_events,
                    onButtonClick = onShowResult,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WordGrid(
    puzzle: WordSearchPuzzle,
    foundCells: Set<WordCell>,
    selection: List<WordCell>,
    accent: Color,
    enabled: Boolean,
    onSelectionChanged: (List<WordCell>) -> Unit,
    onSelectionFinished: () -> Unit
) {
    var startCell by remember { mutableStateOf<WordCell?>(null) }
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(subtleBorder(), shape)
            .padding(6.dp)
            .pointerInput(puzzle, enabled) {
                if (!enabled) return@pointerInput

                val cellSize = size.width / puzzle.size.toFloat()
                detectDragGestures(
                    onDragStart = { offset ->
                        val cell = cellAt(offset, cellSize, puzzle.size)
                        startCell = cell
                        onSelectionChanged(listOf(cell))
                    },
                    onDrag = { change, _ ->
                        val start = startCell
                        if (start != null) {
                            val cell = cellAt(change.position, cellSize, puzzle.size)
                            onSelectionChanged(lineBetween(start, cell))
                        }
                    },
                    onDragEnd = {
                        startCell = null
                        onSelectionFinished()
                    },
                    onDragCancel = {
                        startCell = null
                        onSelectionFinished()
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until puzzle.size) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0 until puzzle.size) {
                        val cell = WordCell(row, col)
                        LetterCell(
                            letter = puzzle.letters[row][col],
                            selected = cell in selection,
                            found = cell in foundCells,
                            accent = accent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterCell(
    letter: Char,
    selected: Boolean,
    found: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> accent
        found -> accent.copy(alpha = 0.16f)
        else -> Color.Transparent
    }
    val textColor = when {
        selected -> OnTile
        found -> accent
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

@Composable
private fun WordChip(
    word: String,
    found: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (found) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (found) {
            AppIcon(icon = R.drawable.ic_check, tint = accent, size = 16.dp)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = word,
            style = MaterialTheme.typography.labelLarge,
            color = if (found) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (found) TextDecoration.LineThrough else null,
            maxLines = 1
        )
    }
}

@Composable
private fun MessageCard(
    @DrawableRes icon: Int,
    tint: Color,
    title: String,
    message: String,
    buttonLabel: String,
    @DrawableRes buttonIcon: Int,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                icon = icon,
                tint = tint,
                size = 68.dp,
                iconSize = 36.dp,
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AppIcon(icon = buttonIcon, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonLabel)
            }
        }
    }
}

private fun cellAt(offset: Offset, cellSize: Float, gridSize: Int): WordCell {
    val col = (offset.x / cellSize).toInt().coerceIn(0, gridSize - 1)
    val row = (offset.y / cellSize).toInt().coerceIn(0, gridSize - 1)
    return WordCell(row = row, col = col)
}

private fun lineBetween(start: WordCell, end: WordCell): List<WordCell> {
    val deltaRow = end.row - start.row
    val deltaCol = end.col - start.col
    val absRow = abs(deltaRow)
    val absCol = abs(deltaCol)

    if (absRow == 0 && absCol == 0) return listOf(start)

    val horizontal = absRow * 2 < absCol
    val vertical = absCol * 2 < absRow

    val stepRow = if (horizontal) 0 else deltaRow.sign
    val stepCol = if (vertical) 0 else deltaCol.sign
    val steps = when {
        horizontal -> absCol
        vertical -> absRow
        else -> maxOf(absRow, absCol)
    }

    return (0..steps).map { step ->
        WordCell(row = start.row + stepRow * step, col = start.col + stepCol * step)
    }
}

@Preview(showBackground = true)
@Composable
private fun CacaPalavrasContentPreview() {
    val puzzle = WordSearchGenerator.generate(
        theme = "Frutas",
        words = listOf("BANANA", "MANGA", "LIMAO", "GOIABA", "PERA", "CAJU"),
        size = 8,
        random = Random(2026)
    )

    DailyPuzzleTheme {
        CacaPalavrasContent(
            uiState = CacaPalavrasUiState(
                loading = false,
                puzzle = puzzle,
                foundWords = setOf("PERA"),
                foundCells = puzzle.words.first { it.word == "PERA" }.cells.toSet()
            ),
            onBack = {},
            onShowResult = {},
            onPlayAgain = {},
            onSelectionChanged = {},
            onSelectionFinished = {}
        )
    }
}
