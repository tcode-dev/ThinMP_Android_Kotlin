package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.service.AlbumDetailService

data class AlbumDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imgUri: Uri = Uri.EMPTY,
    var songs: List<SongModel> = emptyList()
)

class AlbumDetailViewModel(context: Context, id: String) : ViewModel() {
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
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
