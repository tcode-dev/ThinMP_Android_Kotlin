package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.repository.media.SongRepository

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(context: Context) {
    var uiState by mutableStateOf(SongsUiState())
        private set

    init {
        load(context)
    }

    fun load(context: Context) {
        val repository = SongRepository(context)

        uiState.songs = repository.findAll()
    }
}