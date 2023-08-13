package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.service.FavoriteArtistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteArtistsUiState(
    var artists: List<ArtistModel> = emptyList()
)

class FavoriteArtistsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(FavoriteArtistsUiState())
    val uiState: StateFlow<FavoriteArtistsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val repository = FavoriteArtistsService(getApplication())
        val artists = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                artists = artists
            )
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