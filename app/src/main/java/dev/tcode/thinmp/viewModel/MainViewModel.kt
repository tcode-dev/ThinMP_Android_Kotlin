package dev.tcode.thinmp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.constant.MainMenuEnum

data class MainUiState(
    var menuList: List<MainMenuEnum> = MainMenuEnum.values().toList()
)
class MainViewModel : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun load() {
        uiState.menuList = MainMenuEnum.values().toList()
    }
}