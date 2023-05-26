package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.register.FavoriteArtistRegister
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.*
import java.util.*

const val TIME_FORMAT = "%1\$tM:%1\$tS"
const val START_TIME = "00:00"

data class PlayerUiState(
    var songId: SongId = SongId(""),
    var primaryText: String = "",
    var secondaryText: String = "",
    var imageUri: Uri = Uri.EMPTY,
    var sliderPosition: Float = 0f,
    var currentTime: String = START_TIME,
    var durationTime: String = START_TIME,
    var isPlaying: Boolean = false,
    var repeat: RepeatState = RepeatState.OFF,
    var shuffle: Boolean = false,
    var isFavoriteArtist: Boolean = false,
    var isFavoriteSong: Boolean = false,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener, FavoriteArtistRegister, FavoriteSongRegister {
    private var musicPlayer: MusicPlayer
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var timer: Timer? = null

    init {
        musicPlayer = MusicPlayer(application)
        musicPlayer.addEventListener(this)
    }

    fun toggle() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause()
        } else {
            musicPlayer.play()
        }
    }

    fun prev() {
        cancelSeekBarProgressTask()
        musicPlayer.prev()

        if (musicPlayer.isPlaying()) {
            setSeekBarProgressTask()
        }
    }

    fun next() {
        cancelSeekBarProgressTask()
        musicPlayer.next()

        if (musicPlayer.isPlaying()) {
            setSeekBarProgressTask()
        }
    }

    fun seek(value: Float) {
        cancelSeekBarProgressTask()

        val song = musicPlayer.getCurrentSong() ?: return
        val ms = (song.duration.toFloat() * value).toLong()

        musicPlayer.seekTo(ms)

        seekBarProgress()
    }

    fun seekFinished() {
        if (musicPlayer.isPlaying()) {
            setSeekBarProgressTask()
        }
    }

    fun changeRepeat() {
        musicPlayer.changeRepeat()
    }

    fun changeShuffle() {
        musicPlayer.changeShuffle()
    }

    fun favoriteArtist() {
        val song = musicPlayer.getCurrentSong() ?: return

        if (exists(song.artistId)) {
            delete(song.artistId)
        } else {
            add(song.artistId)
        }

        update()
    }

    fun favoriteSong() {
        val song = musicPlayer.getCurrentSong() ?: return

        if (exists(song.songId)) {
            delete(song.songId)
        } else {
            add(song.songId)
        }

        update()
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
//        timer = Timer()
//        timer?.schedule(seekBarProgressTask(), 0, 1000L)
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
                    songId = song.songId,
                    primaryText = song.name,
                    secondaryText = song.artistName,
                    imageUri = song.getImageUri(),
                    sliderPosition = getSliderPosition(),
                    currentTime = String.format(TIME_FORMAT, musicPlayer.getCurrentPosition().toLong()),
                    durationTime = String.format(TIME_FORMAT, song.duration.toLong()),
                    isPlaying = musicPlayer.isPlaying(),
                    repeat = musicPlayer.getRepeat(),
                    shuffle = musicPlayer.getShuffle(),
                    isFavoriteArtist = exists(song.artistId),
                    isFavoriteSong = exists(song.songId)
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