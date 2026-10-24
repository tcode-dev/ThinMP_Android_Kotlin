package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackState(
    val currentSong: SongModel? = null,
    val isPlaying: Boolean = false,
    val repeat: RepeatState = RepeatState.OFF,
    val shuffle: Boolean = false,
    val hasQueue: Boolean = false,
)

/**
 * MusicServiceへのアプリで唯一の接続。
 * MainActivity.onStart()で接続し、onStop()で解放する。
 * 画面はサービスにバインドせず[state]を読むので、開いた時点で再生中の曲がわかる。
 *
 * [state]は接続が切れても残るので、バックグラウンドから戻ったときは、
 * 再接続するまで最後の状態を表示する。
 *
 * MediaControllerのアプリケーションスレッドであるメインスレッドで使う。
 */
@Singleton
class PlaybackController @Inject constructor(@ApplicationContext private val context: Context) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /**
     * 曲が変わらないシーク(3秒を過ぎてからの「前へ」など)では[state]が変わらないので、
     * 一時停止中のプレーヤー画面が位置を更新できるよう、別に通知する。
     */
    private val _positionDiscontinuity = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val positionDiscontinuity: SharedFlow<Unit> = _positionDiscontinuity.asSharedFlow()

    /**
     * サービスが再生できない曲(ファイルが消えた曲)を外したことの通知。
     * 曲リストはこれで再読み込みする。
     * 接続中、つまり画面が見えている間だけ届く。
     */
    private val _songRemoved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val songRemoved: SharedFlow<Unit> = _songRemoved.asSharedFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    /** 未接続の間に呼ばれた操作。捨てずに接続後に順に実行するので、接続前のタップも効く。 */
    private val pending = mutableListOf<(MediaController) -> Unit>()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            update(player)

            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) _positionDiscontinuity.tryEmit(Unit)
        }
    }

    private val controllerListener = object : MediaController.Listener {
        override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            if (command.customAction != MusicService.SONG_REMOVED.customAction) {
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
            }

            _songRemoved.tryEmit(Unit)

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    fun connect() {
        if (controllerFuture != null) return

        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, token).setListener(controllerListener).buildAsync()

        controllerFuture = future
        future.addListener({ onConnected(future) }, ContextCompat.getMainExecutor(context))
    }

    /** [state]と、接続待ちの操作はそのまま残す。 */
    fun release() {
        val future = controllerFuture ?: return

        controllerFuture = null
        controller?.removeListener(listener)
        controller = null
        MediaController.releaseFuture(future)
    }

    fun isConnected(): Boolean {
        return controller != null
    }

    fun start(songs: List<SongModel>, index: Int) {
        if (songs.isEmpty()) return

        whenConnected {
            it.setMediaItems(songs.map { song -> song.toMediaItem(withUri = false) }, index, 0)
            it.prepare()
            it.play()
        }
    }

    fun play() {
        whenConnected { it.play() }
    }

    fun pause() {
        whenConnected { it.pause() }
    }

    fun prev() {
        whenConnected { it.seekToPrevious() }
    }

    fun next() {
        whenConnected { it.seekToNext() }
    }

    fun seekTo(ms: Long) {
        whenConnected { it.seekTo(ms) }
    }

    /** 古いかもしれない[state]ではなく、実行時のプレーヤーのモードから次へ進める。 */
    fun changeRepeat() {
        whenConnected { it.repeatMode = repeatStateOf(it.repeatMode).next().toRepeatMode() }
    }

    fun changeShuffle() {
        whenConnected { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    /** 毎フレーム変わる値なので[state]には入れない。曲がなければ0。 */
    fun currentPosition(): Long {
        val controller = this.controller ?: return 0

        if (controller.mediaItemCount == 0) return 0

        return controller.currentPosition
    }

    /**
     * release()の後に完了したfutureは不要になった接続のもので、
     * コントローラーはreleaseFuture()が解放するので使わない。
     *
     * get()の例外はcatchしない。
     * 自分のアプリのサービスが接続を拒否するならバグだから。
     */
    private fun onConnected(future: ListenableFuture<MediaController>) {
        if (controllerFuture !== future) return

        val controller = future.get()

        this.controller = controller
        controller.addListener(listener)
        update(controller)

        val operations = pending.toList()

        pending.clear()
        operations.forEach { it(controller) }
    }

    private fun whenConnected(operation: (MediaController) -> Unit) {
        val controller = this.controller

        if (controller == null) {
            pending.add(operation)

            return
        }

        operation(controller)
    }

    private fun update(player: Player) {
        val hasQueue = player.mediaItemCount > 0
        val state = PlaybackState(
            currentSong = if (hasQueue) player.currentMediaItem?.toSongModel() else null,
            isPlaying = player.isPlaying,
            repeat = repeatStateOf(player.repeatMode),
            shuffle = player.shuffleModeEnabled,
            hasQueue = hasQueue,
        )

        if (isSame(_state.value, state)) return

        _state.value = state
    }

    /**
     * onEvents()は読み込みやバッファリングでも呼ばれる。
     * SongModelはequalsを持たないのでidで比べ、同じ状態を流さないようにする。
     * 流すとプレーヤー画面がそのたびにお気に入りを問い合わせ直してしまう。
     */
    private fun isSame(a: PlaybackState, b: PlaybackState): Boolean {
        return a.copy(currentSong = null) == b.copy(currentSong = null) && a.currentSong?.id == b.currentSong?.id
    }

    /** Hiltで注入できないview modelやテストから取得するためのもの。 */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Accessor {
        fun playbackController(): PlaybackController
    }

    companion object {
        fun from(context: Context): PlaybackController {
            return EntryPointAccessors.fromApplication(context.applicationContext, Accessor::class.java).playbackController()
        }
    }
}
