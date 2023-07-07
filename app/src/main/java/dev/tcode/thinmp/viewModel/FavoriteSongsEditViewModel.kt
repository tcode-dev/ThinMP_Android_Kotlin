package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.service.FavoriteSongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteSongsEditUiState(
    var songs: List<SongModel> = emptyList()
)

class FavoriteSongsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteSongRegister {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(FavoriteSongsEditUiState())
    val uiState: StateFlow<FavoriteSongsEditUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application)
        load(application)
    }

    fun load(context: Context) {
        val repository = FavoriteSongsService(context)
        val songs = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                songs = songs
            )
        }
    }

    fun start(index: Int) {
        musicPlayer.start(_uiState.asStateFlow().value.songs, index)
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

    fun update() {
        val songIds = uiState.value.songs.map { it.songId }

        update(songIds)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }
}