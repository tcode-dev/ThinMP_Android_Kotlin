package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.constant.MainMenuEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    var menuList: List<MainMenuEnum> = emptyList()
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { currentState ->
            currentState.copy(
                menuList = MainMenuEnum.values().toList()
            )
        }
    }
}