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

    /**
     * Whether load() has put real data in _uiState. save() applies uiState as it stands, and its
     * initial state is empty, so a done tap that beat the load used to write that emptiness back:
     * reorder and replaceAll both take "not in the list" to mean "delete", which is how a list
     * emptied by swiping is saved. Joining the load is not enough on its own - load() can return
     * without populating anything - so the write is guarded on this as well.
     */
    private var loaded = false

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
            loaded = true
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
            loadJob?.join()

            if (loaded) {
                reorderPlaylists(uiState.value.playlists.map { it.id })
            }

            saved.emit(Unit)
        }
    }
}