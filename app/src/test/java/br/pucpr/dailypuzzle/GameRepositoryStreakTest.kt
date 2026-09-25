package br.pucpr.dailypuzzle

import br.pucpr.dailypuzzle.data.local.dao.ProgressDao
import br.pucpr.dailypuzzle.data.local.dao.StatsDao
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.local.entity.GameStatsEntity
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeProgressDao : ProgressDao {

    val rows = mutableListOf<GameProgressEntity>()

    override suspend fun insert(progress: GameProgressEntity) {
        rows += progress
    }

    override suspend fun getByGameAndDate(game: Game, date: String): GameProgressEntity? =
        rows.firstOrNull { it.gameId == game && it.date == date }

    override suspend fun getAllForGame(game: Game): List<GameProgressEntity> =
        rows.filter { it.gameId == game }.sortedByDescending { it.date }

    override fun observeByDate(date: String): Flow<List<GameProgressEntity>> =
        flowOf(rows.filter { it.date == date })

    override fun observeSince(since: String): Flow<List<GameProgressEntity>> =
        flowOf(rows.filter { it.date >= since })
}

private class FakeStatsDao : StatsDao {

    val rows = mutableMapOf<Game, GameStatsEntity>()

    override suspend fun upsert(stats: GameStatsEntity) {
        rows[stats.gameId] = stats
    }

    override suspend fun getByGame(game: Game): GameStatsEntity? = rows[game]

    override fun observeAll(): Flow<List<GameStatsEntity>> = flowOf(rows.values.toList())
}

class GameRepositoryStreakTest {

    private val progressDao = FakeProgressDao()
    private val statsDao = FakeStatsDao()
    private val repository = GameRepository(progressDao, statsDao)

    @Test
    fun diasSeguidosSomamNaSequencia() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.daysAgo(2))
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.daysAgo(1))
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.today())

        val stats = statsDao.rows.getValue(Game.CACA_PALAVRAS)
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
        assertEquals(3, stats.totalPlayed)
        assertEquals(3, stats.totalWon)
    }

    @Test
    fun diaAntigoIsoladoNaoAumentaASequencia() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.daysAgo(3))
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.today())

        val stats = statsDao.rows.getValue(Game.CACA_PALAVRAS)
        assertEquals(1, stats.currentStreak)
        assertEquals(1, stats.bestStreak)
        assertEquals(2, stats.totalPlayed)
    }

    @Test
    fun derrotaZeraASequenciaMasMantemORecorde() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.daysAgo(2))
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.daysAgo(1))
        repository.saveResult(Game.CACA_PALAVRAS, won = false, attempts = 6, date = DateUtils.today())

        val stats = statsDao.rows.getValue(Game.CACA_PALAVRAS)
        assertEquals(0, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
        assertEquals(3, stats.totalPlayed)
        assertEquals(2, stats.totalWon)
    }

    @Test
    fun mesmoDiaNaoEhSalvoDuasVezes() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.today())
        repository.saveResult(Game.CACA_PALAVRAS, won = false, attempts = 9, date = DateUtils.today())

        assertEquals(1, progressDao.rows.size)
        assertEquals(true, progressDao.rows.first().won)
    }
}
