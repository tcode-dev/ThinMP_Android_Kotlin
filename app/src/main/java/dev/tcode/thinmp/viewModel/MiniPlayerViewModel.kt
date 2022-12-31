package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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

class MiniPlayerViewModel( private val context: Context) : ViewModel(), MusicPlayerListener {
    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()
//    var uiState by mutableStateOf(MiniPlayerUiState())
//        private set
//    private var musicPlayer: MusicPlayer

    init {
//        musicPlayer = MusicPlayer(context)
//        musicPlayer.setListener(this)
    }

    fun update() {
//        val song = musicPlayer.getCurrentSong()
//        if (song != null) {
//            Log.d("MiniPlayerViewModel", "true")
//            Log.d("MiniPlayerViewModel", song.name)
//            _uiState.update { currentState ->
//                currentState.copy(
//                    primaryText = song.name,
//                    imageUri = song.getImageUri(),
//                    isVisible = true
//                )
//            }
//        } else {
//            Log.d("MiniPlayerViewModel", "false")
//            _uiState.update { currentState ->
//                currentState.copy(
//                    primaryText = "",
//                    imageUri = Uri.EMPTY,
//                    isVisible = false
//                )
//            }
//        }
    }

    fun forceUpdate() {
        Log.d("MiniPlayerViewModel", "forceUpdate")
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = "testtest",
                    imageUri = Uri.EMPTY,
                    isVisible = true
                )
            }
    }

    override fun onBind() {
        update()
    }
}
