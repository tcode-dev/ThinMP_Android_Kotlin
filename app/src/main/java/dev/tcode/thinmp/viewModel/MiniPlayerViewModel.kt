package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener

data class MiniPlayerUiState(
    var primaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var isVisible: Boolean = false
)

class MiniPlayerViewModel(context: Context) : ViewModel(), MusicPlayerListener {
    private var musicPlayer: MusicPlayer
    var uiState by mutableStateOf(MiniPlayerUiState())
        private set

    init {
        musicPlayer = MusicPlayer(context)
        musicPlayer.setListener(this)
    }

    fun update() {
        val song = musicPlayer.getCurrentSong()
        if (song != null) {
            Log.d("MiniPlayerViewModel", "true")
            Log.d("MiniPlayerViewModel", song.name)
            uiState.primaryText = song.name
            uiState.imageUri = song.getImageUri()
            uiState.isVisible = true
        } else {
            Log.d("MiniPlayerViewModel", "false")
            uiState.primaryText = ""
            uiState.imageUri = Uri.EMPTY
            uiState.isVisible = false
        }
    }

    override fun onBind() {
        update()
    }
}
