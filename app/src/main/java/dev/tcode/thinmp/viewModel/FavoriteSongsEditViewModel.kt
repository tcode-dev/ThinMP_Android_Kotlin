package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.service.FavoriteSongsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * `loaded`が必要な理由はPlaylistsEditUiStateを参照。
 */
data class FavoriteSongsEditUiState(
    val songs: List<SongModel> = emptyList(), val loaded: Boolean = false
)

class FavoriteSongsEditViewModel(application: Application) : AndroidViewModel(application), FavoriteSongRegister {
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    private val _uiState = MutableStateFlow(FavoriteSongsEditUiState())
    val uiState: StateFlow<FavoriteSongsEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

    init {
        load()
    }

    /**
     * PlaylistsEditViewModel.load()を参照。
     */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = FavoriteSongsService(getApplication())
            val songs = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    songs = songs, loaded = true
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

    /**
     * PlaylistsEditViewModel.save()を参照。
     */
    fun save() {
        if (saveJob?.isActive == true) return
        if (!uiState.value.loaded) return

        saveJob = viewModelScope.launch {
            replaceFavoriteSongs(uiState.value.songs.map { it.songId })
            saved.emit(Unit)
        }
    }
}