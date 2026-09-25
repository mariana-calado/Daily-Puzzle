package br.pucpr.dailypuzzle.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class WordBank(
    val theme: String,
    val words: List<String>
)

@Singleton
class WordSearchLoader @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun loadBanks(): List<WordBank> {
        val content = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        val banks = JSONObject(content).getJSONArray("bancos")

        return (0 until banks.length()).map { index ->
            val bank = banks.getJSONObject(index)
            val words = bank.getJSONArray("palavras")
            WordBank(
                theme = bank.getString("tema"),
                words = (0 until words.length()).map { position ->
                    words.getString(position).uppercase()
                }
            )
        }
    }

    private companion object {
        const val FILE_NAME = "caca_palavras_bancos.json"
    }
}
