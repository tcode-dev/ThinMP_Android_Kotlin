package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.service.SongsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application)
        load(application)
    }

    fun start(index: Int) {
        val songs = _uiState.asStateFlow().value.songs

        if (songs.isEmpty()) {
            return
        }

        musicPlayer.start(songs, index)
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
