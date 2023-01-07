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
import java.util.*

data class PlayerUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var currentTime: String = "00:00",
    var durationTime: String = "00:00",
    var isPlaying: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application),
    MusicPlayerListener,
    CustomLifecycleEventObserverListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var timer: Timer? = null

    init {
        musicPlayer = MusicPlayer(application)
        musicPlayer.addEventListener(this)
        setSeekBarProgressTask()
    }

    fun play() {
        musicPlayer.play()
    }

    fun pause() {
        musicPlayer.pause()
    }

    fun prev() {
        musicPlayer.prev()
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

    override fun onDestroy(context: Context) {
        musicPlayer.unbindService(context)
    }

    private fun seekBarProgress() {
        seekBarProgress(musicPlayer.getCurrentPosition())
    }

    /**
     * seekBarProgress
     */
    private fun seekBarProgress(currentPosition: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                currentTime = String.format("%1\$tM:%1\$tS", currentPosition.toLong()),
            )
        }
    }

    /**
     * seekBarProgressTask
     *
     * @return TimerTask
     */
    private fun seekBarProgressTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                seekBarProgress()
            }
        }
    }

    /**
     * setSeekBarProgressTask
     */
    fun setSeekBarProgressTask() {
        timer = Timer()
        timer?.schedule(seekBarProgressTask(), 0, 1000L)
    }

    /**
     * cancelSeekBarProgressTask
     */
    private fun cancelSeekBarProgressTask() {
        if (timer == null) return
        timer!!.cancel()
        timer = null
    }

    private fun update() {
        val song = musicPlayer.getCurrentSong()
        if (song != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = song.name,
                    secondaryText = song.artistName,
                    imageUri = song.getImageUri(),
                    currentTime = "00:00",
                    durationTime = String.format("%1\$tM:%1\$tS", song.duration?.toLong()),
                    isPlaying = musicPlayer.isPlaying()
                )
            }
        } else {
            _uiState.update {
                PlayerUiState()
            }
        }
    }
}
