package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.repository.media.AlbumRepository

data class AlbumDetailUiState(
    var album: AlbumModel? = null
)

class AlbumDetailViewModel(context: Context, id: String) : ViewModel() {
    var uiState by mutableStateOf(AlbumDetailUiState())
        private set

    init {
        load(context, id)
    }

    private fun load(context: Context, id: String) {
        val repository = AlbumRepository(context)

        uiState.album = repository.findById(id)
    }
}