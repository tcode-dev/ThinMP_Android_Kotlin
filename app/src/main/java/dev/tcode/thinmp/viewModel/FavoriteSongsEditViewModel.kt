package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.service.FavoriteSongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteSongsEditUiState(
    var songs: List<SongModel> = emptyList()
)

class FavoriteSongsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteSongRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(FavoriteSongsEditUiState())
    val uiState: StateFlow<FavoriteSongsEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = FavoriteSongsService(getApplication())
            val songs = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    songs = songs
                )
            }
        }
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

    fun save() {
        val songIds = uiState.value.songs.map { it.songId }

        replaceFavoriteSongs(songIds)
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }
}