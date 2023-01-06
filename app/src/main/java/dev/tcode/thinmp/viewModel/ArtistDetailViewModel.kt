package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.service.ArtistDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ArtistDetailUiState(
    var primaryText: String = "", var secondaryText: String = "", var imageUri: Uri = Uri.EMPTY,
    var albums: List<AlbumModel> = emptyList(),
    var songs: List<SongModel> = emptyList()
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()
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
        val service = ArtistDetailService(context)
        val artist = service.findById(id)

        if (artist != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = artist.primaryText,
                    secondaryText = artist.secondaryText,
                    imageUri = artist.imageUri,
                    albums = artist.albums,
                    songs = artist.songs
                )
            }
        }
    }
}
