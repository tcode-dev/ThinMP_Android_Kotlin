package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.register.FavoriteArtistRegister
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    private val INTERVAL_MS = 1000L
    private val musicPlayer: MusicPlayer = MusicPlayer(this)
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            seekBarProgress()
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    init {
        musicPlayer.bindService(application)
    }

    fun toggle() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause()
            cancelSeekBarProgressTask()
        } else {
            musicPlayer.play()
            setSeekBarProgressTask()
        }
    }

    fun prev() {
        cancelSeekBarProgressTask()
        musicPlayer.prev()
        setSeekBarProgressTask()
    }

    fun next() {
        cancelSeekBarProgressTask()
        musicPlayer.next()
        setSeekBarProgressTask()
    }

    fun seek(value: Float) {
        cancelSeekBarProgressTask()

        val song = musicPlayer.getCurrentSong() ?: return
        val ms = (song.duration.toFloat() * value).toLong()

        musicPlayer.seekTo(ms)

        seekBarProgress()
    }

    fun seekFinished() {
        setSeekBarProgressTask()
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
        setSeekBarProgressTask()
    }

    override fun onChange() {
        update()
    }

    override fun onResume(context: Context) {
        if (initialized) {
            musicPlayer.addEventListener(this)
            musicPlayer.bindService(context)
        } else {
            initialized = true
        }
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
        cancelSeekBarProgressTask()
    }

    private fun seekBarProgress() {
        _uiState.update { currentState ->
            currentState.copy(
                sliderPosition = getSliderPosition(),
                currentTime = String.format(TIME_FORMAT, musicPlayer.getCurrentPosition()),
            )
        }
    }

    private fun setSeekBarProgressTask() {
        if (!musicPlayer.isPlaying()) return

        cancelSeekBarProgressTask()
        handler.post(runnable)
    }

    private fun cancelSeekBarProgressTask() {
        handler.removeCallbacks(runnable)
    }

    private fun update() {
        val song = musicPlayer.getCurrentSong() ?: return

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

        setSeekBarProgressTask()
    }

    private fun getSliderPosition(): Float {
        val song = musicPlayer.getCurrentSong() ?: return 0f

        return (musicPlayer.getCurrentPosition().toFloat() / song.duration.toFloat())
    }
}