package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.register.FavoriteArtistRegister
import dev.tcode.thinmp.service.FavoriteArtistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteArtistsEditUiState(
    var artists: List<ArtistModel> = emptyList()
)

class FavoriteArtistsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteArtistRegister {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(FavoriteArtistsEditUiState())
    val uiState: StateFlow<FavoriteArtistsEditUiState> = _uiState.asStateFlow()

    init {
        load(application)
    }

    fun load(context: Context) {
        val repository = FavoriteArtistsService(context)
        val artists = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                artists = artists
            )
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

    fun update() {
        val artistIds = uiState.value.artists.map { it.artistId }

        update(artistIds)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }
}