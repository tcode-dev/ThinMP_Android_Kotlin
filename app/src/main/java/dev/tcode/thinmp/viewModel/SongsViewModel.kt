package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.repository.media.SongRepository

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(context: Context) {
    private var musicPlayer: MusicPlayer
    var uiState by mutableStateOf(SongsUiState())
        private set

    init {
        musicPlayer = MusicPlayer(context)
        load(context)
    }

    fun load(context: Context) {
        val repository = SongRepository(context)

        uiState.songs = repository.findAll()
    }

    fun start(song: SongModel) {
        musicPlayer.start(song)
    }
}
