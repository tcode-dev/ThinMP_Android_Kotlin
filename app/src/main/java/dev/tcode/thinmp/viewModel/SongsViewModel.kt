package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.service.SongsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SongsUiState(
    val songs: List<SongModel> = emptyList()
)

class SongsViewModel(application: Application) : SongListViewModel(application) {
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
