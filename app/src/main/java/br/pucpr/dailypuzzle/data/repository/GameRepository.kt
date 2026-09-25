package br.pucpr.dailypuzzle.data.repository

import br.pucpr.dailypuzzle.data.local.dao.ProgressDao
import br.pucpr.dailypuzzle.data.local.dao.StatsDao
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.local.entity.GameStatsEntity
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.util.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val progressDao: ProgressDao,
    private val statsDao: StatsDao
) {

    fun observeTodayProgress(): Flow<List<GameProgressEntity>> =
        progressDao.observeByDate(DateUtils.today())

    fun observeStats(): Flow<List<GameStatsEntity>> = statsDao.observeAll()

    fun observeHistory(game: Game): Flow<List<GameProgressEntity>> =
        progressDao.observeHistory(game)

    suspend fun getTodayProgress(game: Game): GameProgressEntity? =
        progressDao.getByGameAndDate(game, DateUtils.today())

    suspend fun playedToday(game: Game): Boolean = getTodayProgress(game) != null

    suspend fun getStats(game: Game): GameStatsEntity? = statsDao.getByGame(game)

    suspend fun saveResult(game: Game, won: Boolean, attempts: Int) {
        val today = DateUtils.today()
        if (progressDao.getByGameAndDate(game, today) != null) return

        progressDao.insert(
            GameProgressEntity(
                gameId = game,
                date = today,
                completed = true,
                won = won,
                attempts = attempts
            )
        )
        updateStats(game, won)
    }

    private suspend fun updateStats(game: Game, won: Boolean) {
        val current = statsDao.getByGame(game) ?: GameStatsEntity(gameId = game)
        val wonYesterday = progressDao.getByGameAndDate(game, DateUtils.yesterday())?.won == true
        val newStreak = when {
            !won -> 0
            wonYesterday -> current.currentStreak + 1
            else -> 1
        }

        statsDao.upsert(
            current.copy(
                currentStreak = newStreak,
                bestStreak = maxOf(current.bestStreak, newStreak),
                totalPlayed = current.totalPlayed + 1,
                totalWon = current.totalWon + if (won) 1 else 0
            )
        )
    }
}
