package br.pucpr.dailypuzzle.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta principal do app. O sufixo indica o "tom": 40 = escuro (tema claro), 80 = claro (tema escuro).
val Indigo40 = Color(0xFF4F5BD5)
val Indigo80 = Color(0xFFBAC3FF)
val Slate40 = Color(0xFF5B5D72)
val Slate80 = Color(0xFFC4C5DD)
val Coral40 = Color(0xFFB4533A)
val Coral80 = Color(0xFFFFB59F)

// Cores de feedback compartilhadas pelos jogos de letras (Termo, Hashtag, Caça-palavras...).
// São as mesmas nos temas claro e escuro, para o jogador sempre reconhecer o significado.
val TileCorrect = Color(0xFF3A8F5C) // verde: letra certa no lugar certo / palavra encontrada
val TilePresent = Color(0xFFB7892B) // amarelo: letra existe na palavra, mas em outra posição
val TileAbsent = Color(0xFF787C7E)  // cinza: letra não existe na palavra
val OnTile = Color.White            // cor do texto em cima das três cores acima
