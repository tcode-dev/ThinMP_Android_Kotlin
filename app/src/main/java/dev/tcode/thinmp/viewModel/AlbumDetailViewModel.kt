package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.service.AlbumDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AlbumDetailUiState(
    var primaryText: String = "", var secondaryText: String = "", var imageUri: Uri = Uri.EMPTY, var songs: List<SongModel> = emptyList()
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer()
    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()
    val id: String

    init {
        id = savedStateHandle.get<String>("id").toString()

        musicPlayer.bindService(application)
        load(application)
    }

    fun start(index: Int) {
        musicPlayer.start(getApplication(),_uiState.asStateFlow().value.songs, index)
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            musicPlayer.bindService(context)
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val service = AlbumDetailService(context)
        val album = service.findById(id) ?: return

        _uiState.update { currentState ->
            currentState.copy(
                primaryText = album.primaryText, secondaryText = album.secondaryText, imageUri = album.imageUri, songs = album.songs
            )
        }
    }
}