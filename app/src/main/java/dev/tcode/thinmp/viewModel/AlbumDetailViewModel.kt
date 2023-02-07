package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.service.AlbumDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AlbumDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var songs: List<SongModel> = emptyList()
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteSongRegister {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()
    val id: String

    init {
        musicPlayer = MusicPlayer(application)
        id = savedStateHandle.get<String>("id").toString()

        load(application)
    }

    fun start(index: Int) {
        musicPlayer.start(_uiState.asStateFlow().value.songs, index)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val service = AlbumDetailService(context)
        val album = service.findById(id)

        if (album != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = album.primaryText,
                    secondaryText = album.secondaryText,
                    imageUri = album.imageUri,
                    songs = album.songs
                )
            }
        }
    }
}