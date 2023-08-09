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
    var primaryText: String = "", var imageUri: Uri = Uri.EMPTY, var isVisible: Boolean = false, var isPlaying: Boolean = false
)

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    private var musicPlayer: MusicPlayer = MusicPlayer()
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    init {
        setup(application)
    }

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
        println("Log: MiniPlayerViewModel onBind")
        update()
    }

    override fun onChange() {
        update()
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
    }

    override fun onResume(context: Context) {
        println("Log: MiniPlayerViewModel onResume")
        if (initialized) {
            setup(context)
        } else {
            initialized = true
        }
    }

    private fun setup(context: Context) {
        println("Log: MiniPlayerViewModel setup 1")
        if (!musicPlayer.isServiceRunning()) return
        println("Log: MiniPlayerViewModel setup 2")
        musicPlayer.addEventListener(this)
        musicPlayer.bindService(context)
    }

    private fun update() {
        println("Log: MiniPlayerViewModel update 1")
        val song = musicPlayer.getCurrentSong() ?: return
        println("Log: MiniPlayerViewModel update 2")

        _uiState.update { currentState ->
            currentState.copy(
                primaryText = song.name, imageUri = song.getImageUri(), isVisible = true, isPlaying = musicPlayer.isPlaying()
            )
        }
    }
}