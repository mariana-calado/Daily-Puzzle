package br.pucpr.dailypuzzle.ui.games.cacapalavras

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.data.content.WordSearchGenerator
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.WordCell
import br.pucpr.dailypuzzle.model.WordSearchPuzzle
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme
import br.pucpr.dailypuzzle.ui.theme.OnTile
import br.pucpr.dailypuzzle.ui.theme.gameAccent
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

@Composable
fun CacaPalavrasScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: CacaPalavrasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CacaPalavrasContent(
        uiState = uiState,
        onBack = onBack,
        onFinished = onFinished,
        onSelectionChanged = viewModel::onSelectionChanged,
        onSelectionFinished = viewModel::onSelectionFinished
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CacaPalavrasContent(
    uiState: CacaPalavrasUiState,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onSelectionChanged: (List<WordCell>) -> Unit,
    onSelectionFinished: () -> Unit
) {
    val accent = gameAccent(Game.CACA_PALAVRAS)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Game.CACA_PALAVRAS.title,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.loading || uiState.puzzle == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.alreadyPlayed -> {
                    MessageCard(
                        emoji = "✅",
                        title = "Você já jogou hoje",
                        message = "Volte amanhã para um novo desafio.",
                        buttonLabel = "Voltar ao início",
                        onButtonClick = onBack,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                else -> {
                    GameBoard(
                        puzzle = uiState.puzzle,
                        uiState = uiState,
                        accent = accent,
                        onFinished = onFinished,
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
    onFinished: () -> Unit,
    onSelectionChanged: (List<WordCell>) -> Unit,
    onSelectionFinished: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = puzzle.theme,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text(
                text = "${uiState.foundWords.size}/${puzzle.words.size}",
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WordGrid(
            puzzle = puzzle,
            foundCells = uiState.foundCells,
            selection = uiState.selection,
            accent = accent,
            enabled = !uiState.finished,
            onSelectionChanged = onSelectionChanged,
            onSelectionFinished = onSelectionFinished
        )

        Spacer(modifier = Modifier.height(20.dp))

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
            Spacer(modifier = Modifier.height(8.dp))
            MessageCard(
                emoji = "🎉",
                title = "Parabéns!",
                message = "Você encontrou todas as palavras de hoje.",
                buttonLabel = "Ver resultado",
                onButtonClick = onFinished,
                modifier = Modifier.fillMaxWidth()
            )
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(4.dp)
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
        found -> accent.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) OnTile else MaterialTheme.colorScheme.onSurface
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
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (found) accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
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
    emoji: String,
    title: String,
    message: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onButtonClick) {
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
            onFinished = {},
            onSelectionChanged = {},
            onSelectionFinished = {}
        )
    }
}
