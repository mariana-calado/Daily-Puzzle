package br.pucpr.dailypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(progress: GameProgressEntity)

    @Query("SELECT * FROM game_progress WHERE gameId = :game AND date = :date LIMIT 1")
    suspend fun getByGameAndDate(game: Game, date: String): GameProgressEntity?

    @Query("SELECT * FROM game_progress WHERE gameId = :game ORDER BY date DESC")
    suspend fun getAllForGame(game: Game): List<GameProgressEntity>

    @Query("SELECT * FROM game_progress WHERE date = :date")
    fun observeByDate(date: String): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE date >= :since ORDER BY date DESC")
    fun observeSince(since: String): Flow<List<GameProgressEntity>>
}
