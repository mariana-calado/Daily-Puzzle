package br.pucpr.dailypuzzle.ui.games.cruzadinha

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.common.AppTopBar
import br.pucpr.dailypuzzle.ui.common.WinMessage
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.model.LetterStatus

private val keyboardRows = listOf(
    "QWERTYUIOP",
    "ASDFGHJKL",
    "ZXCVBNM"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CruzadinhaScreen(
    viewModel: CruzadinhaViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAlreadyPlayed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val alreadyPlayed by viewModel.alreadyPlayed.collectAsState()

    // Dia já jogado: vai direto para a tela de resultado
    LaunchedEffect(alreadyPlayed) {
        if (alreadyPlayed == true) onAlreadyPlayed()
    }

    // Casas da palavra selecionada (para destacar)
    val wordCells = remember(state.selRow, state.selCol, state.horizontal) {
        state.currentEntry?.cells()?.toSet().orEmpty()
    }

    // Enquanto verifica (ou redireciona), não mostra o tabuleiro
    if (alreadyPlayed != false) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val puzzle = state.puzzle
    val won = state.gameStatus == GameStatus.WON

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(title = Game.CRUZADINHA_MINI.title, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Grade
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (r in 0 until puzzle.height) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (c in 0 until puzzle.width) {
                                CruzadinhaCell(
                                    letter = state.input[r][c],
                                    number = puzzle.numbers[r to c],
                                    isBlock = puzzle.isBlock(r, c),
                                    isSelected = r == state.selRow && c == state.selCol,
                                    isInWord = (r to c) in wordCells,
                                    isWrong = (r to c) in state.wrong,
                                    isWon = won,
                                    onClick = { viewModel.onCellClick(r, c) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dica atual / mensagem de fim de jogo
                if (won) {
                    WinMessage(text = "Você completou!")
                } else {
                    val entry = state.currentEntry
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onToggleDirection() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (entry != null) {
                                "${entry.number}${if (entry.horizontal) "→" else "↓"}  ${entry.clue}"
                            } else "",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Teclado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keyboardRows.forEachIndexed { index, letters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                    ) {
                        if (index == keyboardRows.lastIndex) {
                            KeyButton(
                                label = "CHECAR",
                                weight = 1.6f,
                                fontSize = 11.sp,
                                onClick = { viewModel.onCheck() }
                            )
                        }
                        letters.forEach { c ->
                            KeyButton(label = c.toString(), onClick = { viewModel.onKeyPress(c) })
                        }
                        if (index == keyboardRows.lastIndex) {
                            KeyButton(
                                label = "⌫",
                                weight = 1.6f,
                                fontSize = 18.sp,
                                onClick = { viewModel.onBackspace() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CruzadinhaCell(
    letter: Char,
    number: Int?,
    isBlock: Boolean,
    isSelected: Boolean,
    isInWord: Boolean,
    isWrong: Boolean,
    isWon: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    val background = when {
        isBlock -> MaterialTheme.colorScheme.onBackground
        isWon -> LetterStatus.CORRECT.color
        isSelected || isInWord -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    val borderColor = when {
        isBlock || isWon -> Color.Transparent
        isWrong -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.onBackground
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = when {
        isWon -> Color.White
        isWrong -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(shape)
            .background(background)
            .border(border = BorderStroke(2.dp, borderColor), shape = shape)
            .then(if (isBlock) Modifier else Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center
    ) {
        if (!isBlock) {
            if (number != null) {
                Text(
                    text = number.toString(),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 5.dp, top = 3.dp)
                )
            }
            if (letter != ' ') {
                Text(
                    text = letter.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun RowScope.KeyButton(
    label: String,
    onClick: () -> Unit,
    weight: Float = 1f,
    fontSize: TextUnit = 16.sp
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}