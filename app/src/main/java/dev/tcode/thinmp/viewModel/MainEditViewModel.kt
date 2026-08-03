package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.register.ShortcutRegister
import dev.tcode.thinmp.service.MainService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainEditUiState(
    var menu: List<MainMenuItem> = emptyList(), var shortcuts: List<ShortcutModel> = emptyList(), var recentlyAlbumsVisibility: Boolean = true, var shortcutVisibility: Boolean = true
)

class MainEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, ShortcutRegister {
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    /** See PlaylistsEditViewModel.loaded. */
    private var loaded = false

    private val _uiState = MutableStateFlow(MainEditUiState())
    val uiState: StateFlow<MainEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = MainService(getApplication())
            val menu = service.getMenu()
            val shortcutVisibility = service.getShortcutVisibility()
            val recentlyAlbumsVisibility = service.getRecentlyAlbumsVisibility()
            val shortcuts = service.getShortcuts()

            _uiState.update { currentState ->
                currentState.copy(
                    menu = menu, shortcuts = shortcuts, recentlyAlbumsVisibility = recentlyAlbumsVisibility, shortcutVisibility = shortcutVisibility
                )
            }
            loaded = true
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

    fun setRecentlyAlbumsVisibility() {
        _uiState.update { currentState ->
            currentState.copy(
                recentlyAlbumsVisibility = !currentState.recentlyAlbumsVisibility
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

    fun removeShortcut(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.shortcuts.toMutableList()

            list.removeAt(index)

            currentState.copy(
                shortcuts = list
            )
        }
    }

    fun save() {
        if (saveJob?.isActive == true) return

        saveJob = viewModelScope.launch {
            loadJob?.join()

            if (loaded) {
                val config = ConfigStore(getApplication())

                uiState.value.menu.forEach {
                    config.saveMainMenuVisibility(it.key, it.visibility)
                }

                config.saveShortcutVisibility(uiState.value.shortcutVisibility)
                config.saveRecentlyAlbumsVisibility(uiState.value.recentlyAlbumsVisibility)

                reorderShortcuts(uiState.value.shortcuts.map { it.id })
            }

            saved.emit(Unit)
        }
    }
}