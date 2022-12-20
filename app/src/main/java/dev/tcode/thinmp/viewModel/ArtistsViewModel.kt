package dev.tcode.thinmp.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.repository.media.ArtistRepository

data class ArtistsUiState(
    var artists: List<ArtistModel> = emptyList()
)

class ArtistsViewModel(context: Context) {
    var uiState by mutableStateOf(ArtistsUiState())
        private set

    init {
        load(context)
    }

    fun load(context: Context) {
        val repository = ArtistRepository(context)

        uiState.artists = repository.findAll()
    }
}