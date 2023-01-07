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

const val TIME_FORMAT = "%1\$tM:%1\$tS"
const val START_TIME = "00:00"

data class PlayerUiState(
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var sliderPosition: Float = 0f,
    var currentTime: String = START_TIME,
    var durationTime: String = START_TIME,
    var isPlaying: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var timer: Timer? = null

    init {
        musicPlayer = MusicPlayer(application)
        musicPlayer.addEventListener(this)
    }

    fun play() {
        musicPlayer.play()
        setSeekBarProgressTask()
    }

    fun pause() {
        musicPlayer.pause()
        cancelSeekBarProgressTask()
    }

    fun prev() {
        musicPlayer.prev()
    }

    fun next() {
        musicPlayer.next()
    }

    override fun onBind() {
        update()

        if (musicPlayer.isPlaying()) {
            setSeekBarProgressTask()
        }
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
        cancelSeekBarProgressTask()
    }

    override fun onDestroy(context: Context) {
        musicPlayer.unbindService(context)
    }

    private fun seekBarProgress() {
        seekBarProgress(musicPlayer.getCurrentPosition())
    }

    private fun seekBarProgress(currentPosition: Int) {
        val song = musicPlayer.getCurrentSong()

        if (song != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    sliderPosition = (musicPlayer.getCurrentPosition().toFloat() / song.duration.toFloat()),
                    currentTime = String.format(TIME_FORMAT, currentPosition.toLong()),
                )
            }
        }
    }

    private fun seekBarProgressTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                seekBarProgress()
            }
        }
    }

    private fun setSeekBarProgressTask() {
        timer = Timer()
        timer?.schedule(seekBarProgressTask(), 0, 1000L)
    }

    private fun cancelSeekBarProgressTask() {
        timer?.cancel()
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
                    sliderPosition = 0f,
                    currentTime = START_TIME,
                    durationTime = String.format(TIME_FORMAT, song.duration.toLong()),
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
