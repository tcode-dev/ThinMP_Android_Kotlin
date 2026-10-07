package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.player.PlaybackState
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

const val START_TIME = "00:00"

data class PlayerUiState(
    val songId: SongId = SongId(""),
    val primaryText: String = "",
    val secondaryText: String = "",
    val imageUri: Uri = Uri.EMPTY,
    val sliderPosition: Float = 0f,
    val currentTime: String = START_TIME,
    val durationTime: String = START_TIME,
    val isPlaying: Boolean = false,
    val repeat: RepeatState = RepeatState.OFF,
    val shuffle: Boolean = false,
    val isFavoriteArtist: Boolean = false,
    val isFavoriteSong: Boolean = false,
)

/**
 * Follows PlaybackController.state. The seek bar is the one thing state does not carry: it is read
 * from currentPosition() once a second while playing, and again whenever the position jumps.
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteArtistRegister,
    FavoriteSongRegister {
    private val INTERVAL_MS = 1000L
    private val playbackController = PlaybackController.from(application)
    private var favoriteJob: Job? = null
    private var seekBarJob: Job? = null
    private var hadQueue = false
    private var isStopped = false
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    val queueEmptied = OneShotEvent<Unit>()

    init {
        // Collected from the current value on, which viewModelScope's immediate dispatcher delivers
        // before the constructor returns, so the first frame already has the song.
        viewModelScope.launch {
            playbackController.state.collect { onState(it) }
        }
        viewModelScope.launch {
            playbackController.positionDiscontinuity.collect { seekBarProgress() }
        }
    }

    fun toggle() {
        if (playbackController.state.value.isPlaying) {
            playbackController.pause()
        } else {
            playbackController.play()
        }
    }

    fun prev() {
        playbackController.prev()
    }

    fun next() {
        playbackController.next()
    }

    fun seek(value: Float) {
        cancelSeekBarProgressTask()

        val song = currentSong() ?: return
        val ms = (song.duration.toFloat() * value).toLong()

        playbackController.seekTo(ms)

        seekBarProgress()
    }

    fun seekFinished() {
        setSeekBarProgressTask()
    }

    fun changeRepeat() {
        playbackController.changeRepeat()
    }

    fun changeShuffle() {
        playbackController.changeShuffle()
    }

    fun favoriteArtist() {
        val song = currentSong() ?: return

        viewModelScope.launch {
            toggleFavoriteArtist(song.artistId)
            updateFavorites(song.artistId, song.songId)
        }
    }

    fun favoriteSong() {
        val song = currentSong() ?: return

        viewModelScope.launch {
            toggleFavoriteSong(song.songId)
            updateFavorites(song.artistId, song.songId)
        }
    }

    override fun onResume() {
        isStopped = false
        setSeekBarProgressTask()
    }

    override fun onStop() {
        isStopped = true
        cancelSeekBarProgressTask()
    }

    /**
     * The queue emptying out under the screen - retry() dropped the song that failed and found
     * nothing left to play - leaves the screen, which is what the mini player does for the same
     * state. Every control here would otherwise reach an empty player and do nothing.
     *
     * When retry() did find another song the queue never empties, and the screen stays.
     */
    private fun onState(state: PlaybackState) {
        cancelSeekBarProgressTask()

        if (hadQueue && !state.hasQueue) {
            viewModelScope.launch { queueEmptied.emit(Unit) }
        }

        hadQueue = state.hasQueue

        val song = state.currentSong ?: return

        _uiState.update { currentState ->
            currentState.copy(
                songId = song.songId,
                primaryText = song.name,
                secondaryText = song.artistName,
                imageUri = song.getImageUri(),
                sliderPosition = getSliderPosition(),
                currentTime = formatTime(playbackController.currentPosition()),
                durationTime = formatTime(song.duration.toLong()),
                isPlaying = state.isPlaying,
                repeat = state.repeat,
                shuffle = state.shuffle
            )
        }

        updateFavorites(song.artistId, song.songId)
        setSeekBarProgressTask()
    }

    private fun currentSong() = playbackController.state.value.currentSong

    private fun seekBarProgress() {
        _uiState.update { currentState ->
            currentState.copy(
                sliderPosition = getSliderPosition(),
                currentTime = formatTime(playbackController.currentPosition()),
            )
        }
    }

    private fun setSeekBarProgressTask() {
        if (isStopped || !playbackController.state.value.isPlaying) return

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

    /**
     * Kept out of the _uiState.update lambda above: update re-runs its lambda when the compare-
     * and-set loses, which would re-issue the queries.
     *
     * Cancelling the previous job matters while skipping tracks, where the song and the playing
     * state change in quick succession. Without it two in-flight queries can complete out of order
     * and paint the previous track's favourite state.
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
        val song = currentSong() ?: return 0f

        return sliderPosition(playbackController.currentPosition(), song.duration.toLong())
    }
}
