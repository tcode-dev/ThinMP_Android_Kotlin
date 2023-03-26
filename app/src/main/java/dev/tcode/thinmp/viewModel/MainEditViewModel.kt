package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.service.MainService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainEditUiState(
    var menu: List<MainMenuEnum> = emptyList()
)

class MainEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private val _uiState = MutableStateFlow(MainEditUiState())
    val uiState: StateFlow<MainEditUiState> = _uiState.asStateFlow()

    init {
        load(application)
    }

    fun load(context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()

        _uiState.update { currentState ->
            currentState.copy(
                menu = menu
            )
        }
    }

    fun setMainMenuVisibility(target: String) {
        _uiState.update { currentState ->
            val menu = currentState.menu.map{
                if (it.key == target) {
                    it.visibility = false
                }
                it
            }

            currentState.copy(
                menu = menu
            )
        }
    }
}