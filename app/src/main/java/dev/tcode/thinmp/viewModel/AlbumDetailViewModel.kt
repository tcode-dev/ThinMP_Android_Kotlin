package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.service.AlbumDetailService

data class AlbumDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var songs: List<SongModel> = emptyList(),
    var shouldShowPlayer: Boolean = false
)

class AlbumDetailViewModel(context: Context, id: String) : ViewModel() {
    private var musicPlayer: MusicPlayer
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
        musicPlayer = MusicPlayer(context)
        load(context, id)
    }

    fun start(index: Int) {
        musicPlayer.start(uiState.songs, index)
    }

    private fun load(context: Context, id: String) {
        val service = AlbumDetailService(context)
        val album = service.findById(id)

        if (album != null) {
            uiState.primaryText = album.primaryText
            uiState.secondaryText = album.secondaryText
            uiState.imageUri = album.imageUri
            uiState.songs = album.songs
            uiState.shouldShowPlayer = musicPlayer.isActive()
        }
    }
}
