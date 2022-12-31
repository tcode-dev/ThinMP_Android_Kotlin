package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MiniPlayerUiState(
    var primaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var isVisible: Boolean = false
)

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application as Context)
        musicPlayer.setListener(this)
    }

    fun update() {
        val song = musicPlayer.getCurrentSong()
        if (song != null) {
            Log.d("MiniPlayerViewModel", "true")
            Log.d("MiniPlayerViewModel", song.name)
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = song.name,
                    imageUri = song.getImageUri(),
                    isVisible = true
                )
            }
        } else {
            Log.d("MiniPlayerViewModel", "false")
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = "",
                    imageUri = Uri.EMPTY,
                    isVisible = false
                )
            }
        }
    }

    override fun onBind() {
        update()
    }
}
