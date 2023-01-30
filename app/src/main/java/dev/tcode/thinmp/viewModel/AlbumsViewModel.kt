package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.service.AlbumsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AlbumsUiState(
    var albums: List<AlbumModel> = emptyList()
)

class AlbumsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    init {
        load(application)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val service = AlbumsService(context)
        val albums = service.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                albums = albums
            )
        }
    }
}