package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.SongRepository

data class AlbumDetailUiState(
    var album: AlbumModel? = null,
    var songs: List<SongModel> = emptyList()
)

class AlbumDetailViewModel(context: Context, id: String) : ViewModel() {
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
        load(context, id)
    }

    private fun load(context: Context, id: String) {
        val albumRepository = AlbumRepository(context)
        val songRepository = SongRepository(context)

        uiState.album = albumRepository.findById(id)
        uiState.songs = songRepository.findByAlbumId(id)
    }
}