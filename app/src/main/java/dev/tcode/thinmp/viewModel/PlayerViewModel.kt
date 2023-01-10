package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.config.ConfigDataStore
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.*
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
    var isPlaying: Boolean = false,
    var repeat: Int = 0,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var timer: Timer? = null
    private val config: ConfigDataStore

    init {
        musicPlayer = MusicPlayer(application)
        musicPlayer.addEventListener(this)
        config = ConfigDataStore(application)
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

    fun seek(value: Float) {
        cancelSeekBarProgressTask()

        val song = musicPlayer.getCurrentSong() ?: return
        val msec = (song.duration.toFloat() * value).toInt()

        musicPlayer.seekTo(msec)

        seekBarProgress()
    }

    fun seekFinished() {
        if (musicPlayer.isPlaying()) {
            setSeekBarProgressTask()
        }
    }

    fun setRepeat() {
        musicPlayer.setRepeat()
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
        _uiState.update { currentState ->
            currentState.copy(
                sliderPosition = getSliderPosition(),
                currentTime = String.format(TIME_FORMAT, musicPlayer.getCurrentPosition().toLong()),
            )
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
                    sliderPosition = getSliderPosition(),
                    currentTime = String.format(TIME_FORMAT, musicPlayer.getCurrentPosition().toLong()),
                    durationTime = String.format(TIME_FORMAT, song.duration.toLong()),
                    isPlaying = musicPlayer.isPlaying(),
                    repeat = config.getRepeat()
                )
            }
        } else {
            _uiState.update {
                PlayerUiState()
            }
        }
    }

    private fun getSliderPosition(): Float {
        val song = musicPlayer.getCurrentSong() ?: return 0f

        return (musicPlayer.getCurrentPosition().toFloat() / song.duration.toFloat())
    }
}
