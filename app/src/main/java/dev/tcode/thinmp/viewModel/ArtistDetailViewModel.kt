package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.service.ArtistDetailService

data class ArtistDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var albums: List<AlbumModel> = emptyList(),
    var songs: List<SongModel> = emptyList()
)

class ArtistDetailViewModel(context: Context, id: String) : ViewModel() {
    var uiState by mutableStateOf(ArtistDetailUiState())
        private set

    init {
        load(context, id)
    }

    private fun load(context: Context, id: String) {
        val service = ArtistDetailService(context)
        val artist = service.findById(id)

        if (artist != null) {
            uiState.primaryText = artist.primaryText
            uiState.secondaryText = artist.secondaryText
            uiState.imageUri = artist.imageUri
            uiState.albums = artist.albums
            uiState.songs = artist.songs
        }
    }
}
