package br.pucpr.dailypuzzle.data.repository

import br.pucpr.dailypuzzle.data.content.WordSearchGenerator
import br.pucpr.dailypuzzle.data.content.WordSearchLoader
import br.pucpr.dailypuzzle.model.DailySeed
import br.pucpr.dailypuzzle.model.WordSearchPuzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordSearchRepository @Inject constructor(
    private val loader: WordSearchLoader
) {

    suspend fun getPuzzle(date: String): WordSearchPuzzle = withContext(Dispatchers.IO) {
        val banks = loader.loadBanks()
        val bank = banks[DailySeed.indexFor(date, banks.size)]

        WordSearchGenerator.generate(
            theme = bank.theme,
            words = bank.words.take(WORDS_PER_PUZZLE),
            size = GRID_SIZE,
            random = DailySeed.randomFor(date)
        )
    }

    private companion object {
        const val GRID_SIZE = 8
        const val WORDS_PER_PUZZLE = 6
    }
}
