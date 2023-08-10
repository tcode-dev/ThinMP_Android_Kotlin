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
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.service.ArtistDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ArtistDetailUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var albums: List<AlbumModel> = emptyList(),
    var songs: List<SongModel> = emptyList(),
    var isVisiblePlayer: Boolean = false
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer()
    private val _uiState = MutableStateFlow(ArtistDetailUiState())
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()
    val id: String

    init {
        id = savedStateHandle.get<String>("id").toString()

        load(application)
        setup(application)
    }

    fun start(index: Int) {
        if (!musicPlayer.isServiceRunning()) {
            musicPlayer.addEventListener(this)
        }

        musicPlayer.start(getApplication(), _uiState.asStateFlow().value.songs, index)
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            setup(context)
            load(context)
        } else {
            initialized = true
        }
    }

    override fun onBind() {
        updateIsVisiblePlayer()
    }

    private fun setup(context: Context) {
        if (!musicPlayer.isServiceRunning()) return

        musicPlayer.addEventListener(this)
        musicPlayer.bindService(context)
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
                    songs = artist.songs,
                    isVisiblePlayer = musicPlayer.isServiceRunning()
                )
            }
        }
    }

    private fun updateIsVisiblePlayer() {
        _uiState.update { currentState ->
            currentState.copy(
                isVisiblePlayer = musicPlayer.isServiceRunning()
            )
        }
    }
}