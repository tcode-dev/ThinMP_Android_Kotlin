package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaylistsUiState(
    var playlists: List<PlaylistModel> = emptyList()
)

class PlaylistsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

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
        val repository = PlaylistsService(getApplication())
        val playlists = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                playlists = playlists
            )
        }
    }
}