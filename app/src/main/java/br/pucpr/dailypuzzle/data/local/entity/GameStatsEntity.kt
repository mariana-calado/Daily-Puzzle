package br.pucpr.dailypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.pucpr.dailypuzzle.model.Game

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val gameId: Game,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalPlayed: Int = 0,
    val totalWon: Int = 0
)
