package br.pucpr.dailypuzzle.ui.games.termo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.model.LetterStatus

private val keyboardRows = listOf(
    "QWERTYUIOP",
    "ASDFGHJKL",
    "ZXCVBNM"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermoScreen(
    viewModel: TermoViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Melhor status de cada letra já tentada (CORRECT > PRESENT > ABSENT)
    val keyStatuses: Map<Char, LetterStatus> = remember(state.grid) {
        val priority = listOf(LetterStatus.ABSENT, LetterStatus.PRESENT, LetterStatus.CORRECT)
        val map = mutableMapOf<Char, LetterStatus>()
        state.grid.flatten().forEach { letter ->
            if (letter.status in priority) {
                val current = map[letter.char]
                if (current == null || priority.indexOf(letter.status) > priority.indexOf(current)) {
                    map[letter.char] = letter.status
                }
            }
        }
        map
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "TERMO",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grade de letras
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.grid.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { letter ->
                                LetterCell(
                                    char = letter.char.toString(),
                                    status = letter.status
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mensagem de fim de jogo
                when (state.gameStatus) {
                    GameStatus.WON -> Text(
                        text = "Você venceu! 🎉",
                        color = Color(0xFF2E7D32),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    GameStatus.LOST -> Text(
                        text = "Fim de jogo! A palavra era ${state.targetWord}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    GameStatus.PLAYING -> Spacer(modifier = Modifier.height(28.dp))
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
                        // Última linha: Enter à esquerda e Apagar à direita
                        if (index == keyboardRows.lastIndex) {
                            KeyButton(
                                label = "ENTER",
                                weight = 1.6f,
                                fontSize = 11.sp,
                                onClick = { viewModel.onEnter() }
                            )
                        }
                        letters.forEach { c ->
                            KeyButton(
                                label = c.toString(),
                                status = keyStatuses[c],
                                onClick = { viewModel.onKeyPress(c) }
                            )
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
private fun LetterCell(char: String, status: LetterStatus) {
    // EMPTY e TYPED ficam só com contorno; as demais são avaliadas e preenchidas
    val isEvaluated = status != LetterStatus.EMPTY && status != LetterStatus.TYPED
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(shape)
            .background(if (isEvaluated) status.color else Color.Transparent)
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    color = when {
                        isEvaluated -> Color.Transparent
                        status == LetterStatus.TYPED -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEvaluated) Color.White else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun RowScope.KeyButton(
    label: String,
    onClick: () -> Unit,
    weight: Float = 1f,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    status: LetterStatus? = null
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(status?.color ?: MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = if (status != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}