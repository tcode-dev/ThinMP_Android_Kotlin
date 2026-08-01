package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.register.FavoriteArtistRegister
import dev.tcode.thinmp.service.FavoriteArtistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteArtistsEditUiState(
    var artists: List<ArtistModel> = emptyList()
)

class FavoriteArtistsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteArtistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private val _uiState = MutableStateFlow(FavoriteArtistsEditUiState())
    val uiState: StateFlow<FavoriteArtistsEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

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

    fun removeArtist(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.artists.toMutableList()

            list.removeAt(index)

            currentState.copy(
                artists = list
            )
        }
    }

    fun save() {
        if (saveJob?.isActive == true) return

        saveJob = viewModelScope.launch {
            replaceFavoriteArtists(uiState.value.artists.map { it.artistId })
            saved.emit(Unit)
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