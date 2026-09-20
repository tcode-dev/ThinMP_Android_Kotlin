package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MiniPlayerUiState(
    val primaryText: String = "", val imageUri: Uri = Uri.EMPTY, val isVisible: Boolean = false, val isPlaying: Boolean = false
)

class MiniPlayerViewModel(application: Application) : MusicPlayerViewModel(application) {
    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    fun toggle() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause()
        } else {
            musicPlayer.play()
        }
    }

    fun next() {
        musicPlayer.next()
    }

    override fun onBind() {
        update()
    }

    override fun onChange() {
        update()
    }

    override fun onError() {
        _uiState.update { currentState ->
            currentState.copy(
                isVisible = false
            )
        }
    }

    private fun update() {
        val song = musicPlayer.getCurrentSong() ?: return

        _uiState.update { currentState ->
            currentState.copy(
                primaryText = song.name, imageUri = song.getImageUri(), isVisible = true, isPlaying = musicPlayer.isPlaying()
            )
        }
    }
}