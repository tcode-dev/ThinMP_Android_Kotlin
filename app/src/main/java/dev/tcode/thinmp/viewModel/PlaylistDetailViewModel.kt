package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.service.PlaylistDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PlaylistDetailUiState(
    var primaryText: String = "", var secondaryText: String = "", var imageUri: Uri = Uri.EMPTY, var songs: List<SongModel> = emptyList(), var isVisiblePlayer: Boolean = false
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener, MusicPlayerListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer(this)
    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()
    val id: PlaylistId

    init {
        id = PlaylistId(savedStateHandle.get<String>("id").toString())

        load()
        bindService()
    }

    fun start(index: Int) {
        musicPlayer.start(getApplication(), _uiState.asStateFlow().value.songs, index)
    }

    override fun onStop() {
        musicPlayer.destroy(getApplication())
    }

    override fun onResume() {
        if (initialized) {
            load()
            bindService()
        } else {
            initialized = true
        }
    }

    override fun onBind() {
        updateIsVisiblePlayer()
    }

    private fun bindService() {
        if (musicPlayer.isServiceRunning()) {
            musicPlayer.bindService(getApplication())
        }
    }

    private fun load() {
        val service = PlaylistDetailService(getApplication())
        val playlist = service.findById(id)

        if (playlist != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = playlist.primaryText, secondaryText = playlist.secondaryText, imageUri = playlist.imageUri, songs = playlist.songs, isVisiblePlayer = musicPlayer.isServiceRunning()
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