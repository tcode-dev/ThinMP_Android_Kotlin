package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.service.AlbumDetailService
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AlbumDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var songs: List<SongModel> = emptyList(),
    var shouldShowPlayer: Boolean = false
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), MusicPlayerListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application, this)

        savedStateHandle.get<String>("id")?.let {
            load(application, it)
        }
    }

    fun start(index: Int) {
        val songs = _uiState.asStateFlow().value.songs

        if (songs.isEmpty()) {
            return
        }

        musicPlayer.start(songs, index)

        _uiState.update { currentState ->
            currentState.copy(
                shouldShowPlayer = true
            )
        }
    }

    private fun load(context: Context, id: String) {
        val service = AlbumDetailService(context)
        val album = service.findById(id)

        if (album != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = album.primaryText,
                    secondaryText = album.secondaryText,
                    imageUri = album.imageUri,
                    songs = album.songs,
                    shouldShowPlayer = musicPlayer.isActive()
                )
            }
        }
    }
}
