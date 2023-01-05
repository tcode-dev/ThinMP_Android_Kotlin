package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    var menuList: List<MainMenuEnum> = emptyList()
)

class MainViewModel : ViewModel(), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }

    private fun load() {
        _uiState.update { currentState ->
            currentState.copy(
                menuList = MainMenuEnum.values().toList()
            )
        }
    }
}
