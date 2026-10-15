package br.pucpr.dailypuzzle.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.ui.theme.DailyPuzzleTheme

@Composable
fun EmBreveScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = title, onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconBadge(
                icon = R.drawable.ic_construction,
                tint = MaterialTheme.colorScheme.primary,
                size = 96.dp,
                iconSize = 48.dp,
                shape = CircleShape
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Em breve",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Este jogo ainda está em construção.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(onClick = onBack) {
                AppIcon(icon = R.drawable.ic_home, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Voltar ao início")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmBreveScreenPreview() {
    DailyPuzzleTheme {
        EmBreveScreen(title = "Termo", onBack = {})
    }
}
