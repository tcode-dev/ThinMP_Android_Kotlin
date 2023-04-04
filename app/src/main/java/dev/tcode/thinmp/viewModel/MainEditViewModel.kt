package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.service.MainService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainEditUiState(
    var menu: List<MainMenuItem> = emptyList(),
    var shortcutVisibility: Boolean = true
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
        val shortcutVisibility = service.getShortcutVisibility()

        _uiState.update { currentState ->
            currentState.copy(
                menu = menu,
                shortcutVisibility = shortcutVisibility
            )
        }
    }

    fun setMainMenuVisibility(target: String) {
        _uiState.update { currentState ->
            val map = currentState.menu.map {
                if (it.key == target) {
                    MainMenuItem(it.id, it.key, !it.visibility)
                } else {
                    it
                }
            }
            currentState.copy(
                menu = map
            )
        }
    }

    fun setShortcutVisibility() {
        _uiState.update { currentState ->
            currentState.copy(
                shortcutVisibility = !currentState.shortcutVisibility
            )
        }
    }

    fun save(context: Context) {
        val config = ConfigStore(context)

        uiState.value.menu.forEach {
            config.saveMainMenuVisibility(it.key, it.visibility)
        }

        config.saveShortcutVisibility(uiState.value.shortcutVisibility)
    }
}