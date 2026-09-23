package br.pucpr.dailypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.pucpr.dailypuzzle.model.Game

/**
 * Tabela "game_progress": uma linha por (jogo, dia) com o resultado do desafio daquele dia.
 *
 * O índice único em (gameId, date) garante no próprio banco a regra "um jogo por dia":
 * não é possível existir dois registros do mesmo jogo na mesma data.
 */
@Entity(
    tableName = "game_progress",
    indices = [Index(value = ["gameId", "date"], unique = true)]
)
data class GameProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Game,
    val date: String, // formato "yyyy-MM-dd"
    val completed: Boolean = false,
    val won: Boolean = false,
    val attempts: Int = 0
)
