package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.service.SongsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SongsUiState(
    val songs: List<SongModel> = emptyList()
)

@HiltViewModel
class SongsViewModel @Inject constructor(application: Application, playbackController: PlaybackController) : SongListViewModel(application, playbackController) {
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    override val songs: List<SongModel>
        get() = uiState.value.songs

    init {
        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = SongsService(getApplication())
            val songs = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    songs = songs
                )
            }
        }
    }
}
