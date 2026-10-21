package br.pucpr.dailypuzzle

import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.util.DateUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test
    fun apagarHistoricoRemoveSoOsDadosDoJogoEscolhido() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 3, date = DateUtils.today())
        repository.saveResult(Game.TERMO, won = true, attempts = 2, date = DateUtils.today())

        repository.clearHistory(Game.CACA_PALAVRAS)

        assertEquals(null, repository.getProgress(Game.CACA_PALAVRAS, DateUtils.today()))
        assertEquals(null, repository.getStats(Game.CACA_PALAVRAS))
        assertEquals(true, repository.getProgress(Game.TERMO, DateUtils.today())?.won)
        assertEquals(1, repository.getStats(Game.TERMO)?.totalWon)
    }

    @Test
    fun depoisDeApagarOJogoPodeSerJogadoDeNovoNaqueleDia() = runTest {
        repository.saveResult(Game.CACA_PALAVRAS, won = false, attempts = 6, date = DateUtils.today())
        repository.clearHistory(Game.CACA_PALAVRAS)

        repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = 5, date = DateUtils.today())

        assertEquals(true, repository.getProgress(Game.CACA_PALAVRAS, DateUtils.today())?.won)
        assertEquals(1, repository.getStats(Game.CACA_PALAVRAS)?.currentStreak)
    }
}
