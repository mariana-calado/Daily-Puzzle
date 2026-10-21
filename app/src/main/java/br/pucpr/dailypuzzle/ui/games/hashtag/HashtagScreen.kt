package br.pucpr.dailypuzzle.ui.games.hashtag

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.model.LetterStatus
import br.pucpr.dailypuzzle.util.DailyHashtags
import br.pucpr.dailypuzzle.util.HashtagPuzzle

private val CellSize = 58.dp
private val CellGap = 6.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagScreen(
    viewModel: HashtagViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAlreadyPlayed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val alreadyPlayed by viewModel.alreadyPlayed.collectAsState()

    // Dia já jogado: vai direto para a tela de resultado
    LaunchedEffect(alreadyPlayed) {
        if (alreadyPlayed == true) onAlreadyPlayed()
    }

    val statuses = remember(state.grid) { state.puzzle.statuses(state.grid) }

    // Enquanto verifica (ou redireciona), não mostra o tabuleiro
    if (alreadyPlayed != false) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HASHTAG",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar para a tela inicial"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Movimentos restantes
            Text(
                text = if (state.movesLeft == 1) "1 movimento restante" else "${state.movesLeft} movimentos restantes",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            HashtagBoard(
                puzzle = state.puzzle,
                grid = state.grid,
                statuses = statuses,
                selected = state.selected,
                onTap = viewModel::onCellTap,
                onDragStart = viewModel::onDragStart,
                onDrop = viewModel::onDrop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensagem de fim de jogo
            when (state.gameStatus) {
                GameStatus.WON -> Text(
                    text = "Você venceu em ${DailyHashtags.MAX_MOVES - state.movesLeft} movimentos! 🎉",
                    color = Color(0xFF2E7D32),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                GameStatus.LOST -> Text(
                    text = "Fim de jogo! As palavras eram ${state.puzzle.words.joinToString(", ")}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                GameStatus.PLAYING -> Text(
                    text = "Toque ou arraste para trocar as letras",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HashtagBoard(
    puzzle: HashtagPuzzle,
    grid: List<List<Char>>,
    statuses: Map<Pair<Int, Int>, LetterStatus>,
    selected: Pair<Int, Int>?,
    onTap: (Pair<Int, Int>) -> Unit,
    onDragStart: (Pair<Int, Int>) -> Unit,
    onDrop: (Pair<Int, Int>?, Pair<Int, Int>?) -> Unit
) {
    val density = LocalDensity.current
    val cellPx = with(density) { CellSize.toPx() }
    val step = cellPx + with(density) { CellGap.toPx() }

    // Converte uma posição de toque na casa (linha, coluna) do #; null se cair fora
    fun cellAt(offset: Offset): Pair<Int, Int>? {
        if (offset.x < 0f || offset.y < 0f) return null
        val c = (offset.x / step).toInt()
        val r = (offset.y / step).toInt()
        if (r !in 0 until HashtagPuzzle.SIZE || c !in 0 until HashtagPuzzle.SIZE) return null
        if (offset.x - c * step > cellPx || offset.y - r * step > cellPx) return null
        return if (puzzle.isActive(r, c)) r to c else null
    }

    Column(
        modifier = Modifier
            .pointerInput(step) {
                detectTapGestures { offset -> cellAt(offset)?.let(onTap) }
            }
            .pointerInput(step) {
                var origin: Pair<Int, Int>? = null
                var last = Offset.Zero
                detectDragGestures(
                    onDragStart = { offset ->
                        origin = cellAt(offset)
                        last = offset
                        origin?.let(onDragStart)
                    },
                    onDrag = { change, _ -> last = change.position },
                    onDragEnd = {
                        onDrop(origin, cellAt(last))
                        origin = null
                    },
                    onDragCancel = {
                        onDrop(null, null)
                        origin = null
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(CellGap)
    ) {
        for (r in 0 until HashtagPuzzle.SIZE) {
            Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
                for (c in 0 until HashtagPuzzle.SIZE) {
                    if (puzzle.isActive(r, c)) {
                        HashtagCell(
                            letter = grid[r][c],
                            status = statuses[r to c] ?: LetterStatus.ABSENT,
                            isSelected = selected == (r to c)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(CellSize))
                    }
                }
            }
        }
    }
}

@Composable
private fun HashtagCell(letter: Char, status: LetterStatus, isSelected: Boolean) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .size(CellSize)
            .clip(shape)
            .background(status.color)
            .border(
                border = BorderStroke(
                    width = 3.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}