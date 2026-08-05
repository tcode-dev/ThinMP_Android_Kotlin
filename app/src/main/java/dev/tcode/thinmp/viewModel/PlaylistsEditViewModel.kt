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

/**
 * `loaded` is false until load() has put real data here. The done button is disabled while it is,
 * because save() writes this state as it stands and reorder/replaceAll both read "not in the list"
 * as "delete" - which is how a list emptied by swiping is saved, and what turned a tap that beat
 * the load into a wipe of the whole table. It cannot be inferred from the list being empty: an
 * empty list is also what the user leaves behind after swiping everything away.
 */
data class PlaylistsEditUiState(
    var playlists: List<PlaylistModel> = emptyList(), var loaded: Boolean = false
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
                    playlists = playlists, loaded = true
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

    /**
     * The done button is disabled until the load lands, so this guard is only reached if a tap
     * slips through before the state reaches the screen. Nothing happens then - no write, and no
     * saved event either, because navigating away from a tap the user cannot see the effect of is
     * worse than the tap appearing to do nothing.
     */
    fun save() {
        if (saveJob?.isActive == true) return
        if (!uiState.value.loaded) return

        saveJob = viewModelScope.launch {
            reorderPlaylists(uiState.value.playlists.map { it.id })
            saved.emit(Unit)
        }
    }
}