package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.service.FavoriteArtistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FavoriteArtistsUiState(
    val artists: List<ArtistModel> = emptyList()
)

@HiltViewModel
class FavoriteArtistsViewModel @Inject constructor(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private val _uiState = MutableStateFlow(FavoriteArtistsUiState())
    val uiState: StateFlow<FavoriteArtistsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = FavoriteArtistsService(getApplication())
            val artists = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    artists = artists
                )
            }
        }
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }
}