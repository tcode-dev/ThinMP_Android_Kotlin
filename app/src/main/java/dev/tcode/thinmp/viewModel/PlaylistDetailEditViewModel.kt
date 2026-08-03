package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** See PlaylistsEditUiState for why `loaded` cannot be inferred from the list being empty. */
data class PlaylistDetailEditUiState(
    var primaryText: String = "", var songs: List<SongModel> = emptyList(), var loaded: Boolean = false
)

@HiltViewModel
class PlaylistDetailEditViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    private val _uiState = MutableStateFlow(PlaylistDetailEditUiState())
    val uiState: StateFlow<PlaylistDetailEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()
    val id: PlaylistId

    init {
        id = PlaylistId(savedStateHandle.get<String>("id").toString())

        load()
    }

    fun removeSong(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.songs.toMutableList()

            list.removeAt(index)

            currentState.copy(
                songs = list
            )
        }
    }

    fun changeName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                primaryText = name
            )
        }
    }

    /** See PlaylistsEditViewModel.save(). */
    fun save() {
        if (saveJob?.isActive == true) return
        if (!uiState.value.loaded) return

        saveJob = viewModelScope.launch {
            updatePlaylist(id, uiState.value.primaryText, uiState.value.songs.map { it.songId })
            saved.emit(Unit)
        }
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = PlaylistDetailService(getApplication())
            val playlist = service.findById(id) ?: return@launch

            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = playlist.primaryText, songs = playlist.songs, loaded = true
                )
            }
        }
    }
}