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

    fun observeProgressSince(since: String): Flow<List<GameProgressEntity>> =
        progressDao.observeSince(since)

    fun observeStats(): Flow<List<GameStatsEntity>> = statsDao.observeAll()

    suspend fun getProgress(game: Game, date: String): GameProgressEntity? =
        progressDao.getByGameAndDate(game, date)

    suspend fun getTodayProgress(game: Game): GameProgressEntity? =
        getProgress(game, DateUtils.today())

    suspend fun getStats(game: Game): GameStatsEntity? = statsDao.getByGame(game)

    suspend fun saveResult(
        game: Game,
        won: Boolean,
        attempts: Int,
        date: String = DateUtils.today()
    ) {
        if (progressDao.getByGameAndDate(game, date) != null) return

        progressDao.insert(
            GameProgressEntity(
                gameId = game,
                date = date,
                completed = true,
                won = won,
                attempts = attempts
            )
        )
        recomputeStats(game)
    }

    private suspend fun recomputeStats(game: Game) {
        val history = progressDao.getAllForGame(game)
        val wonDates = history.filter { it.won }.map { it.date }.toSet()

        val today = DateUtils.today()
        val wonToday = today in wonDates
        val lostToday = history.any { it.date == today } && !wonToday

        var currentStreak = 0
        if (!lostToday) {
            var step = if (wonToday) 0 else 1
            while (DateUtils.daysAgo(step) in wonDates) {
                currentStreak++
                step++
            }
        }

        var bestStreak = 0
        var run = 0
        var previous: String? = null
        for (date in wonDates.sorted()) {
            run = if (previous != null && DateUtils.nextDay(previous) == date) run + 1 else 1
            bestStreak = maxOf(bestStreak, run)
            previous = date
        }

        statsDao.upsert(
            GameStatsEntity(
                gameId = game,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalPlayed = history.size,
                totalWon = history.count { it.won }
            )
        )
    }
}
