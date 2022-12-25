package dev.tcode.thinmp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.constant.MainMenuEnum

data class MainUiState(
    var menuList: List<MainMenuEnum> = emptyList()
)

class MainViewModel : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    init {
        load()
    }

    fun load() {
        uiState.menuList = MainMenuEnum.values().toList()
    }
}