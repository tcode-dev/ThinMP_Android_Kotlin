package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.service.AlbumsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AlbumsUiState(
    var albums: List<AlbumModel> = emptyList()
)

class AlbumsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = AlbumsService(getApplication())
            val albums = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    albums = albums
                )
            }
        }
    }
}