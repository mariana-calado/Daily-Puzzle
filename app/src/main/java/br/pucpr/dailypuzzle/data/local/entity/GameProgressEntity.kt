package br.pucpr.dailypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.pucpr.dailypuzzle.model.Game

@Entity(
    tableName = "game_progress",
    indices = [Index(value = ["gameId", "date"], unique = true)]
)
data class GameProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Game,
    val date: String,
    val completed: Boolean = false,
    val won: Boolean = false,
    val attempts: Int = 0
)
