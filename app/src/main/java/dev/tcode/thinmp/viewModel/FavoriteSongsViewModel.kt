package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.repository.realm.FavoriteSongRepository
import dev.tcode.thinmp.service.SongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteSongsUiState(
    var songs: List<SongModel> = emptyList()
)

class FavoriteSongsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application)
        load(application)
    }

    fun start(index: Int) {
        musicPlayer.start(_uiState.asStateFlow().value.songs, index)
    }

    fun deleteFavorite(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.delete(songId)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val repository = SongsService(context)
        val songs = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                songs = songs
            )
        }
    }
}