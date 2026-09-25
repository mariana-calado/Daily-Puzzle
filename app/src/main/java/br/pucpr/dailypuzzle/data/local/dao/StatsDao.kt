package br.pucpr.dailypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.pucpr.dailypuzzle.data.local.entity.GameStatsEntity
import br.pucpr.dailypuzzle.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Upsert
    suspend fun upsert(stats: GameStatsEntity)

    @Query("SELECT * FROM game_stats WHERE gameId = :game LIMIT 1")
    suspend fun getByGame(game: Game): GameStatsEntity?

    @Query("SELECT * FROM game_stats")
    fun observeAll(): Flow<List<GameStatsEntity>>
}
