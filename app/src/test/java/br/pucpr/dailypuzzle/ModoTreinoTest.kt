package br.pucpr.dailypuzzle

import androidx.lifecycle.SavedStateHandle
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.navigation.Routes
import br.pucpr.dailypuzzle.ui.common.attemptsText
import br.pucpr.dailypuzzle.ui.common.winSummary
import br.pucpr.dailypuzzle.ui.games.cruzadinha.CruzadinhaViewModel
import br.pucpr.dailypuzzle.ui.games.hashtag.HashtagViewModel
import br.pucpr.dailypuzzle.ui.games.sudoku.SudokuViewModel
import br.pucpr.dailypuzzle.ui.games.termo.TermoViewModel
import br.pucpr.dailypuzzle.util.DailyWords
import br.pucpr.dailypuzzle.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModoTreinoTest {

    private val progress = FakeProgressDao()
    private val stats = FakeStatsDao()
    private val repository = GameRepository(progress, stats)
    private val hoje = DateUtils.today()

    @Before
    fun abrirDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun fecharDispatcher() {
        Dispatchers.resetMain()
    }

    private fun resultadoAnterior(game: Game, won: Boolean, attempts: Int, date: String = hoje) {
        progress.rows += GameProgressEntity(
            gameId = game,
            date = date,
            completed = true,
            won = won,
            attempts = attempts
        )
    }

    private fun digitarPalavra(viewModel: TermoViewModel, palavra: String) {
        palavra.forEach { viewModel.onKeyPress(it) }
        viewModel.onEnter()
    }

    @Test
    fun termoNaoJogadoNaoTemResultadoAnterior() = runTest {
        val viewModel = TermoViewModel(repository, SavedStateHandle())

        assertEquals(false, viewModel.alreadyPlayed.value)
        assertNull(viewModel.previousResult.value)
        assertFalse(viewModel.practice.value)
        assertTrue(viewModel.isToday)
    }

    @Test
    fun vencerNoTermoSalvaOResultado() = runTest {
        val viewModel = TermoViewModel(repository, SavedStateHandle())

        digitarPalavra(viewModel, DailyWords.forDate(hoje))

        assertEquals(GameStatus.WON, viewModel.state.value.gameStatus)
        assertEquals(1, progress.rows.size)
        assertTrue(progress.rows.first().won)
        assertEquals(1, stats.rows.getValue(Game.TERMO).totalWon)
    }

    @Test
    fun termoJaJogadoMostraOResultadoSalvo() = runTest {
        resultadoAnterior(Game.TERMO, won = false, attempts = 6)

        val viewModel = TermoViewModel(repository, SavedStateHandle())

        assertEquals(true, viewModel.alreadyPlayed.value)
        assertNotNull(viewModel.previousResult.value)
        assertFalse(viewModel.previousResult.value!!.won)
    }

    @Test
    fun jogarNovamenteReiniciaOJogoSemMexerNoResultadoSalvo() = runTest {
        resultadoAnterior(Game.TERMO, won = false, attempts = 6)
        val viewModel = TermoViewModel(repository, SavedStateHandle())

        viewModel.onPlayAgain()
        assertTrue(viewModel.practice.value)
        assertEquals(0, viewModel.state.value.currentRow)

        digitarPalavra(viewModel, DailyWords.forDate(hoje))

        assertEquals(GameStatus.WON, viewModel.state.value.gameStatus)
        assertEquals(1, progress.rows.size)
        assertFalse(progress.rows.first().won)
        assertTrue(stats.rows.isEmpty())
    }

    @Test
    fun jogarNovamenteSemJogoPreviamenteConcluidoTambemNaoSalva() = runTest {
        val viewModel = TermoViewModel(repository, SavedStateHandle())

        viewModel.onPlayAgain()
        digitarPalavra(viewModel, DailyWords.forDate(hoje))

        assertTrue(progress.rows.isEmpty())
    }

    @Test
    fun diaPassadoUsaADataDaRotaEMostraQueNaoEHoje() = runTest {
        val passado = "2026-01-05"
        val handle = SavedStateHandle(mapOf(Routes.ARG_DATE to passado))
        val viewModel = TermoViewModel(repository, handle)

        assertFalse(viewModel.isToday)

        digitarPalavra(viewModel, DailyWords.forDate(passado))

        assertEquals(passado, progress.rows.single().date)
    }

    @Test
    fun hashtagSudokuECruzadinhaTambemTrazemOResultadoEEntramEmTreino() = runTest {
        resultadoAnterior(Game.HASHTAG, won = true, attempts = 7)
        resultadoAnterior(Game.SUDOKU, won = false, attempts = 3)
        resultadoAnterior(Game.CRUZADINHA_MINI, won = true, attempts = 2)

        val hashtag = HashtagViewModel(repository, SavedStateHandle())
        val sudoku = SudokuViewModel(repository, SavedStateHandle())
        val cruzadinha = CruzadinhaViewModel(repository, SavedStateHandle())

        assertTrue(hashtag.previousResult.value!!.won)
        assertFalse(sudoku.previousResult.value!!.won)
        assertTrue(cruzadinha.previousResult.value!!.won)

        hashtag.onPlayAgain()
        sudoku.onPlayAgain()
        cruzadinha.onPlayAgain()

        assertTrue(hashtag.practice.value)
        assertTrue(sudoku.practice.value)
        assertTrue(cruzadinha.practice.value)
        assertEquals(3, progress.rows.size)
    }

    @Test
    fun textosDeResultadoUsamAUnidadeCertaDeCadaJogo() {
        assertEquals("1 tentativa", attemptsText(Game.TERMO, 1))
        assertEquals("4 tentativas", attemptsText(Game.CACA_PALAVRAS, 4))
        assertEquals("1 movimento", attemptsText(Game.HASHTAG, 1))
        assertEquals("9 movimentos", attemptsText(Game.HASHTAG, 9))
        assertEquals("sem erros", attemptsText(Game.SUDOKU, 0))
        assertEquals("2 erros", attemptsText(Game.SUDOKU, 2))

        assertEquals("Você completou o Sudoku sem errar.", winSummary(Game.SUDOKU, 0))
        assertEquals("Você completou o Sudoku com 2 erros.", winSummary(Game.SUDOKU, 2))
        assertEquals("Você reordenou as letras em 5 movimentos.", winSummary(Game.HASHTAG, 5))
    }
}
