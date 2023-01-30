package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.realm.FavoriteArtistRepository
import dev.tcode.thinmp.service.ArtistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ArtistsUiState(
    var artists: List<ArtistModel> = emptyList()
)

class ArtistsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(ArtistsUiState())
    val uiState: StateFlow<ArtistsUiState> = _uiState.asStateFlow()

    init {
        load(application)
    }

    fun existsFavorite(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    fun addFavorite(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.add(artistId)
    }

    fun deleteFavorite(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.delete(artistId)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val service = ArtistsService(context)
        val artists = service.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                artists = artists
            )
        }
    }
}