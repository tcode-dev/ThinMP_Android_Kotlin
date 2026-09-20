package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.service.ArtistDetailService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ArtistDetailUiState(
    val primaryText: String = "",
    val secondaryText: String = "",
    val imageUri: Uri = Uri.EMPTY,
    val albums: List<AlbumModel> = emptyList(),
    val songs: List<SongModel> = emptyList()
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : SongListViewModel(application) {
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()
    val id: String

    override val songs: List<SongModel>
        get() = uiState.value.songs

    init {
        id = savedStateHandle.get<String>("id").toString()

        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = ArtistDetailService(getApplication())
            val artist = service.findById(id) ?: return@launch

            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = artist.primaryText,
                    secondaryText = artist.secondaryText,
                    imageUri = artist.imageUri,
                    albums = artist.albums,
                    songs = artist.songs
                )
            }
        }
    }
}
