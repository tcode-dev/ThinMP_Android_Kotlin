package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaylistsEditUiState(
    var playlists: List<PlaylistModel> = emptyList()
)

class PlaylistsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private val _uiState = MutableStateFlow(PlaylistsEditUiState())
    val uiState: StateFlow<PlaylistsEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

    init {
        load()
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = PlaylistsService(getApplication())
            val playlists = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    playlists = playlists
                )
            }
        }
    }

    fun removePlaylist(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.playlists.toMutableList()

            list.removeAt(index)

            currentState.copy(
                playlists = list
            )
        }
    }

    fun save() {
        if (saveJob?.isActive == true) return

        saveJob = viewModelScope.launch {
            reorderPlaylists(uiState.value.playlists.map { it.id })
            saved.emit(Unit)
        }
    }
}