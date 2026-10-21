package br.pucpr.dailypuzzle

import br.pucpr.dailypuzzle.data.local.dao.ProgressDao
import br.pucpr.dailypuzzle.data.local.dao.StatsDao
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.local.entity.GameStatsEntity
import br.pucpr.dailypuzzle.model.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeProgressDao : ProgressDao {

    val rows = mutableListOf<GameProgressEntity>()

    override suspend fun insert(progress: GameProgressEntity) {
        rows += progress
    }

    override suspend fun getByGameAndDate(game: Game, date: String): GameProgressEntity? =
        rows.firstOrNull { it.gameId == game && it.date == date }

    override suspend fun getAllForGame(game: Game): List<GameProgressEntity> =
        rows.filter { it.gameId == game }.sortedByDescending { it.date }

    override suspend fun deleteByGame(game: Game) {
        rows.removeAll { it.gameId == game }
    }

    override fun observeByDate(date: String): Flow<List<GameProgressEntity>> =
        flowOf(rows.filter { it.date == date })

    override fun observeSince(since: String): Flow<List<GameProgressEntity>> =
        flowOf(rows.filter { it.date >= since })
}

internal class FakeStatsDao : StatsDao {

    val rows = mutableMapOf<Game, GameStatsEntity>()

    override suspend fun upsert(stats: GameStatsEntity) {
        rows[stats.gameId] = stats
    }

    override suspend fun getByGame(game: Game): GameStatsEntity? = rows[game]

    override suspend fun deleteByGame(game: Game) {
        rows.remove(game)
    }

    override fun observeAll(): Flow<List<GameStatsEntity>> = flowOf(rows.values.toList())
}
