package dev.tcode.thinmp.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
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

class AlbumsViewModel() : ViewModel(), CustomLifecycleEventObserverListener {
    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    override fun onResume(context: Context) {
        val service = AlbumsService(context)
        val albums = service.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                albums = albums
            )
        }
    }
}
