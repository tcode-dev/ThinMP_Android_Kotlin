package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.service.AlbumDetailService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AlbumDetailUiState(
    val primaryText: String = "", val secondaryText: String = "", val imageUri: Uri = Uri.EMPTY, val songs: List<SongModel> = emptyList()
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : SongListViewModel(application) {
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()
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
            val service = AlbumDetailService(getApplication())
            val album = service.findById(id) ?: return@launch

            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = album.primaryText, secondaryText = album.secondaryText, imageUri = album.imageUri, songs = album.songs
                )
            }
        }
    }
}
