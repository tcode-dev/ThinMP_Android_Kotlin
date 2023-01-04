package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MiniPlayerUiState(
    var primaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var isVisible: Boolean = false,
    var isPlaying: Boolean = false
)

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application),
    MusicPlayerListener,
    CustomLifecycleEventObserverListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    init {
        musicPlayer = MusicPlayer(application)
        musicPlayer.addEventListener(this)
    }

    fun update() {
        val song = musicPlayer.getCurrentSong()
        if (song != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = song.name,
                    imageUri = song.getImageUri(),
                    isVisible = true,
                    isPlaying = musicPlayer.isPlaying()
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = "",
                    imageUri = Uri.EMPTY,
                    isVisible = false,
                    isPlaying = false
                )
            }
        }
    }

    fun play() {
        musicPlayer.play()
    }

    fun pause() {
        musicPlayer.pause()
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

    override fun onResume(context: Context) {
        musicPlayer.addEventListener(this)
        update()
    }

    override fun onStop() {
        musicPlayer.removeEventListener()
    }
}
