package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.service.AlbumsService

data class AlbumsUiState(
    var albums: List<AlbumModel> = emptyList()
)

class AlbumsViewModel(context: Context) {
    var uiState by mutableStateOf(AlbumsUiState())
        private set

    init {
        load(context)
    }

    fun load(context: Context) {
        val service = AlbumsService(context)

        uiState.albums = service.findAll()
    }
}
