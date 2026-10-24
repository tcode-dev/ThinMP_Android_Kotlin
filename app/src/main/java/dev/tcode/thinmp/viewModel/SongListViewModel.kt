package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 曲を一覧表示し、そこから再生を始める画面のview model。
 * 一覧は画面に戻ったときと、再生できない曲をサービスが外したときに読み込み直す。
 * キューに曲があればミニプレーヤーを表示する。
 *
 * [isVisiblePlayer]はPlaybackControllerの現在の状態から始まるので、
 * 初めて開いた画面でも最初のフレームからミニプレーヤーを表示できる。
 *
 * initで読み込み済みなので、直後に来る最初のON_RESUMEでは読み込まない。
 */
abstract class SongListViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private val playbackController = PlaybackController.from(application)
    private var initialized: Boolean = false

    val isVisiblePlayer: StateFlow<Boolean> =
        playbackController.state.map { it.hasQueue }.stateIn(viewModelScope, SharingStarted.Eagerly, playbackController.state.value.hasQueue)

    /** [start]で再生する一覧。 */
    protected abstract val songs: List<SongModel>

    protected abstract fun load()

    init {
        // initが終わるまでは何も流れないので、サブクラスのフィールドが初期化される前にload()が走ることはない。
        viewModelScope.launch {
            playbackController.songRemoved.collect { load() }
        }
    }

    /**
     * 行の位置ではなくidを受け取る。
     * 一覧は画面に戻ったときや再生エラーの後に読み込み直すので、
     * 表示時の位置はタップした時点では別の曲を指しているかもしれない。
     * 一覧にない曲なら何もしない。
     */
    fun start(songId: SongId) {
        val songs = this.songs
        val index = indexOfSong(songs, songId) ?: return

        playbackController.start(songs, index)
    }

    override fun onResume() {
        if (!initialized) {
            initialized = true

            return
        }

        load()
    }
}

/** [songs]の中の[songId]の位置。見つからなければnull。 */
fun indexOfSong(songs: List<SongModel>, songId: SongId): Int? {
    val index = songs.indexOfFirst { it.songId == songId }

    return if (index < 0) null else index
}
