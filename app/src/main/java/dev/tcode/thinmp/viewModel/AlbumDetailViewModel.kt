package dev.tcode.thinmp.viewModel

import android.content.Context
import android.content.ServiceConnection
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.service.AlbumDetailService
import dev.tcode.thinmp.view.screen.PlayInterface

data class AlbumDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imgUri: Uri = Uri.EMPTY,
    var songs: List<SongModel> = emptyList()
)

class AlbumDetailViewModel(context: Context, id: String) : PlayInterface {
    override lateinit var musicService: MusicService
    override lateinit var connection: ServiceConnection
    override var bound: Boolean = false
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
        bindService(context)
        load(context, id)
    }

    private fun load(context: Context, id: String) {
        val service = AlbumDetailService(context)
        val album = service.findById(id)

        if (album != null) {
            uiState.primaryText = album.primaryText
            uiState.secondaryText = album.secondaryText
            uiState.imgUri = album.imgUri
            uiState.songs = album.songs
        }
    }
}
