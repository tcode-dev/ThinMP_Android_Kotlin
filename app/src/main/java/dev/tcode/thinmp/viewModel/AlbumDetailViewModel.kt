package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.repository.media.AlbumRepository

data class AlbumDetailUiState(
    var albums: List<AlbumModel> = emptyList()
)

class AlbumDetailViewModel(context: Context) {
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
        load(context)
    }

    fun load(context: Context) {
        val repository = AlbumRepository(context)

        uiState.albums = repository.findAll()
    }
}