package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel
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
import javax.inject.Inject

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
 * PlaybackController.stateに追従する。
 * stateにないシークバーの位置は、再生中は1秒ごと、位置が飛んだときはその都度currentPosition()から読む。
 * ドラッグ中は指の位置を表示し、再生位置では上書きしない。
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(application: Application, private val playbackController: PlaybackController) : AndroidViewModel(application), CustomLifecycleEventObserverListener, FavoriteArtistRegister,
    FavoriteSongRegister {
    private val INTERVAL_MS = 1000L
    private var favoriteJob: Job? = null
    private var seekBarJob: Job? = null
    private var hadQueue = false
    private var isStopped = false

    /**
     * ドラッグ中の位置(0〜1)で、ドラッグしていないときはnull。
     * 1歩ごとにシークすると音が飛び続けて聞き取れないので、離したときに1回だけシークする。
     */
    private var dragPosition: Float? = null
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    val queueEmptied = OneShotEvent<Unit>()

    init {
        // viewModelScopeはimmediateなので、現在値はコンストラクターが戻る前に届き、
        // 最初のフレームから曲を表示できる。
        viewModelScope.launch {
            playbackController.state.collect { onState(it) }
        }
        viewModelScope.launch {
            playbackController.positionDiscontinuity.collect { seekBarProgress() }
        }
    }

    fun toggle() {
        if (playbackController.state.value.showsPauseButton) {
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

        dragPosition = value
        _uiState.update { currentState ->
            currentState.copy(
                sliderPosition = value,
                currentTime = formatTime(positionMs(song, value)),
            )
        }
    }

    fun seekFinished() {
        val value = dragPosition ?: return

        dragPosition = null

        val song = currentSong()

        if (song != null) {
            playbackController.seekTo(positionMs(song, value))
            seekBarProgress()
        }

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
            refreshFavorites()
        }
    }

    fun favoriteSong() {
        val song = currentSong() ?: return

        viewModelScope.launch {
            toggleFavoriteSong(song.songId)
            refreshFavorites()
        }
    }

    override fun onResume() {
        isStopped = false
        setSeekBarProgressTask()
    }

    /**
     * 画面を離れるとonValueChangeFinishedが来ないことがあるので、ドラッグは取り消す。
     * 残すと、戻ったときにシークバーが止まったままになる。
     */
    override fun onStop() {
        isStopped = true
        dragPosition = null
        cancelSeekBarProgressTask()
    }

    /**
     * 画面を開いている間にキューが空になったら(retry()で再生できる曲がなくなったら)画面を閉じる。
     * ミニプレーヤーも同じ状態で消える。
     * 開いたままだと、どの操作も空のプレーヤーに届くだけになる。
     *
     * retry()が次の曲を見つけた場合はキューが空にならないので、画面はそのまま。
     */
    private fun onState(state: PlaybackState) {
        cancelSeekBarProgressTask()

        if (hadQueue && !state.hasQueue) {
            viewModelScope.launch { queueEmptied.emit(Unit) }
        }

        hadQueue = state.hasQueue

        val song = state.currentSong ?: return
        val isSongChanged = _uiState.value.songId != song.songId
        val isDragging = dragPosition != null

        _uiState.update { currentState ->
            currentState.copy(
                songId = song.songId,
                primaryText = song.name,
                secondaryText = song.artistName,
                imageUri = song.getImageUri(),
                sliderPosition = if (isDragging) currentState.sliderPosition else getSliderPosition(),
                currentTime = if (isDragging) currentState.currentTime else formatTime(playbackController.currentPosition()),
                durationTime = formatTime(durationMs(song)),
                isPlaying = state.showsPauseButton,
                repeat = state.repeat,
                shuffle = state.shuffle
            )
        }

        // 再生状態はシークのたびにバッファリングを挟んで変わるので、そのたびにRoomを読み直さない。
        if (isSongChanged) {
            updateFavorites(song.artistId, song.songId)
        }

        setSeekBarProgressTask()
    }

    private fun currentSong() = playbackController.state.value.currentSong

    /**
     * 切り替えの書き込みが終わるまでに曲が進んでいることがあるので、押した曲ではなく今の曲を読み直す。
     * 押した曲で読むと、次の曲の読み込みをキャンセルして、次の曲の画面に前の曲の状態を出してしまう。
     * 次の曲が同じアーティストなら、次の曲の読み込みが書き込みより先に終わっていることもあるので、読み直しは省かない。
     */
    private fun refreshFavorites() {
        val song = currentSong() ?: return

        updateFavorites(song.artistId, song.songId)
    }

    /**
     * 長さも読み直す。
     * 曲が変わった時点ではプレーヤーがまだファイルを読んでおらず、MediaStoreの長さを表示していることがあるからだ。
     */
    private fun seekBarProgress() {
        if (dragPosition != null) return

        val song = currentSong()

        _uiState.update { currentState ->
            currentState.copy(
                sliderPosition = getSliderPosition(),
                currentTime = formatTime(playbackController.currentPosition()),
                durationTime = if (song == null) currentState.durationTime else formatTime(durationMs(song)),
            )
        }
    }

    private fun setSeekBarProgressTask() {
        if (isStopped || dragPosition != null || !playbackController.state.value.isPlaying) return

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
     * _uiState.updateのラムダの中では呼ばない。
     * updateはcompare-and-setに失敗するとラムダをやり直すので、クエリも投げ直されてしまう。
     *
     * 曲送りで曲と再生状態が続けて変わると、クエリが順不同で終わって
     * 前の曲のお気に入り状態を表示することがあるので、前のジョブはキャンセルする。
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

        return sliderPosition(playbackController.currentPosition(), durationMs(song))
    }

    private fun positionMs(song: SongModel, value: Float): Long {
        return (durationMs(song).toFloat() * value).toLong()
    }

    /**
     * MediaStoreの長さはタグから読んだもので、タグがないファイルでは0になる。
     * プレーヤーがファイルから長さを知っていればそちらを使い、読み込む前だけMediaStoreの値を使う。
     */
    private fun durationMs(song: SongModel): Long {
        return playbackController.duration().takeIf { it > 0 } ?: song.duration.toLong()
    }
}
