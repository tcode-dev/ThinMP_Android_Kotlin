package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.tcode.thinmp.player.MusicPlayer

data class MiniPlayerUiState(
    var primaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var isVisible: Boolean = false
)

class MiniPlayerViewModel(context: Context) : ViewModel() {
    private var musicPlayer: MusicPlayer
    var uiState by mutableStateOf(MiniPlayerUiState())
        private set

    init {
        musicPlayer = MusicPlayer(context)
        update()
    }

    fun update() {


//        uiState.songs = repository.findAll()
    }
}
