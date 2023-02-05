package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaylistsUiState(
    var playlists: List<PlaylistRealmModel> = emptyList()
)

class PlaylistsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

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

    private fun load(context: Context) {
        val repository = PlaylistsService(context)
        val playlists = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                playlists = playlists
            )
        }
    }
}