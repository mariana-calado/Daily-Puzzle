package br.pucpr.dailypuzzle.ui.home

import androidx.lifecycle.ViewModel
import br.pucpr.dailypuzzle.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tudo que a tela inicial precisa desenhar. Por enquanto só a lista de jogos;
 * quando o Repository existir, entra aqui também quais jogos já foram jogados hoje.
 */
data class HomeUiState(
    val games: List<Game> = Game.entries
)

/**
 * ViewModel da Home (o "VM" do MVVM). Ele guarda o estado da tela e sobrevive a
 * mudanças de configuração, como girar o celular: a tela é recriada, o ViewModel não.
 *
 * O estado sai daqui por um StateFlow, que é só de leitura para a tela.
 * Quem pode alterar é o _uiState (privado), dentro do ViewModel.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
