package br.pucpr.dailypuzzle.ui.games.sudoku

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.ui.common.AppTopBar
import br.pucpr.dailypuzzle.ui.common.WinMessage
import br.pucpr.dailypuzzle.model.SudokuState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    viewModel: SudokuViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onAlreadyPlayed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val alreadyPlayed by viewModel.alreadyPlayed.collectAsState()

    LaunchedEffect(alreadyPlayed) {
        if (alreadyPlayed == true) onAlreadyPlayed()
    }

    if (alreadyPlayed != false) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = Game.SUDOKU.title,
                onBack = onBack,
                actions = {
                    TextButton(onClick = { viewModel.onReset() }) {
                        Text("Reiniciar")
                    }
                }
            )
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
                SudokuBoard(
                    state = state,
                    onCellClick = { r, c -> viewModel.onCellSelected(r, c) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (state.isCompleted) {
                    WinMessage(text = "Parabéns! Você completou o Sudoku!")
                } else {
                    Text(
                        text = "Erros: ${state.mistakes}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Teclado numérico
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..5).forEach { n ->
                        NumberKey(label = n.toString(), onClick = { viewModel.onNumberInput(n) })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (6..9).forEach { n ->
                        NumberKey(label = n.toString(), onClick = { viewModel.onNumberInput(n) })
                    }
                    NumberKey(label = "⌫", fontSize = 18.sp, onClick = { viewModel.onErase() })
                }
            }
        }
    }
}

@Composable
private fun SudokuBoard(
    state: SudokuState,
    onCellClick: (Int, Int) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val thickLine = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
    val thinLine = MaterialTheme.colorScheme.outlineVariant

    val selectedBg = MaterialTheme.colorScheme.primaryContainer
    val relatedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val errorBg = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .border(2.dp, thickLine, shape)
    ) {
        for (row in 0 until 9) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (col in 0 until 9) {
                    val cell = state.grid[row][col]
                    val selRow = state.selectedRow
                    val selCol = state.selectedCol
                    val isSelected = row == selRow && col == selCol
                    val isRelated = selRow != null && selCol != null && !isSelected && (
                            row == selRow || col == selCol ||
                                    (row / 3 == selRow / 3 && col / 3 == selCol / 3)
                            )

                    val background = when {
                        isSelected -> selectedBg
                        cell.isError -> errorBg
                        isRelated -> relatedBg
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        cell.isError -> MaterialTheme.colorScheme.error
                        cell.isGiven -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(background)
                            .drawBehind {
                                val thin = 0.5.dp.toPx()
                                val thick = 2.dp.toPx()
                                if (col < 8) {
                                    val w = if (col % 3 == 2) thick else thin
                                    drawLine(
                                        color = if (col % 3 == 2) thickLine else thinLine,
                                        start = Offset(size.width - w / 2, 0f),
                                        end = Offset(size.width - w / 2, size.height),
                                        strokeWidth = w
                                    )
                                }
                                if (row < 8) {
                                    val w = if (row % 3 == 2) thick else thin
                                    drawLine(
                                        color = if (row % 3 == 2) thickLine else thinLine,
                                        start = Offset(0f, size.height - w / 2),
                                        end = Offset(size.width, size.height - w / 2),
                                        strokeWidth = w
                                    )
                                }
                            }
                            .clickable { onCellClick(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell.value != 0) {
                            Text(
                                text = cell.value.toString(),
                                fontSize = 22.sp,
                                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NumberKey(
    label: String,
    onClick: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp
) {
    Box(
        modifier = Modifier
            .weight(1f)
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