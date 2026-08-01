package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.register.FavoriteArtistRegister
import dev.tcode.thinmp.register.FavoriteSongRegister
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private var favoriteJob: Job? = null
    private var seekBarJob: Job? = null
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        bindService()
    }

    fun toggle() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause()
        } else {
            musicPlayer.play()
        }
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

        viewModelScope.launch {
            toggleFavoriteArtist(song.artistId)
            updateFavorites(song.artistId, song.songId)
        }
    }

    fun favoriteSong() {
        val song = musicPlayer.getCurrentSong() ?: return

        viewModelScope.launch {
            toggleFavoriteSong(song.songId)
            updateFavorites(song.artistId, song.songId)
        }
    }

    override fun onBind() {
        update()
    }

    override fun onChange() {
        cancelSeekBarProgressTask()
        update()
    }

    override fun onResume() {
        if (initialized) {
            bindService()
        } else {
            initialized = true
        }
    }

    override fun onStop() {
        musicPlayer.destroy(getApplication())
        cancelSeekBarProgressTask()
    }

    private fun bindService() {
        if (musicPlayer.isServiceRunning()) {
            musicPlayer.bindService(getApplication())
        }
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
        seekBarJob = viewModelScope.launch {
            while (true) {
                seekBarProgress()
                delay(INTERVAL_MS)
            }
        }
    }

    private fun cancelSeekBarProgressTask() {
        seekBarJob?.cancel()
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
                shuffle = musicPlayer.getShuffle()
            )
        }

        updateFavorites(song.artistId, song.songId)
        setSeekBarProgressTask()
    }

    /**
     * Kept out of the _uiState.update lambda above: update re-runs its lambda when the compare-
     * and-set loses, which would re-issue the queries.
     *
     * Cancelling the previous job matters while skipping tracks, where onMediaItemTransition and
     * EVENT_IS_PLAYING_CHANGED call onChange() back to back. Without it two in-flight queries can
     * complete out of order and paint the previous track's favourite state.
     */
    private fun updateFavorites(artistId: ArtistId, songId: SongId) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            val isFavoriteArtist = existsFavoriteArtist(artistId)
            val isFavoriteSong = existsFavoriteSong(songId)

            _uiState.update { currentState ->
                currentState.copy(
                    isFavoriteArtist = isFavoriteArtist, isFavoriteSong = isFavoriteSong
                )
            }
        }
    }

    private fun getSliderPosition(): Float {
        val song = musicPlayer.getCurrentSong() ?: return 0f

        return (musicPlayer.getCurrentPosition().toFloat() / song.duration.toFloat())
    }
}