package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaylistsEditUiState(
    var playlists: List<PlaylistModel> = emptyList()
)

class PlaylistsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(PlaylistsEditUiState())
    val uiState: StateFlow<PlaylistsEditUiState> = _uiState.asStateFlow()

    init {
        load(application)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    fun load(context: Context) {
        val repository = PlaylistsService(context)
        val playlists = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                playlists = playlists
            )
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

    fun update() {
        val playlistIds = uiState.value.playlists.map { it.id }

        updatePlaylists(playlistIds)
    }
}