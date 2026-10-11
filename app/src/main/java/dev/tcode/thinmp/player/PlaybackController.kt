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
 * The app's one connection to MusicService: a MediaController, connected in MainActivity.onStart()
 * and released in onStop(). Screens read [state] instead of binding the service themselves, so a
 * screen has the current song from the moment it is created, and comes back to the last state seen
 * rather than to whatever it held when it was left.
 *
 * [state] outlives the connection. After the app returns from the background it shows the last
 * state until the new connection replaces it.
 *
 * Everything runs on the main thread, which is the controller's application thread.
 */
@Singleton
class PlaybackController @Inject constructor(@ApplicationContext private val context: Context) {
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /**
     * A seek that keeps the song - "previous" past the first three seconds - changes nothing in
     * [state], and a paused player screen would go on showing the old position.
     */
    private val _positionDiscontinuity = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val positionDiscontinuity: SharedFlow<Unit> = _positionDiscontinuity.asSharedFlow()

    /**
     * The service dropped a song it could not play - its file is gone - and the song lists reload
     * without it. Only heard while connected, which is while the screens are visible.
     */
    private val _songRemoved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val songRemoved: SharedFlow<Unit> = _songRemoved.asSharedFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    /**
     * Operations asked for while there is no connection yet. They run in order once it is up rather
     * than being dropped, which is what used to happen to a tap made while a screen was still
     * binding.
     */
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

    /** Keeps [state] as it is, and keeps any operation still waiting for the next connection. */
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

    /** Steps from the mode the player has when the change runs, not from a state that may be stale. */
    fun changeRepeat() {
        whenConnected { it.repeatMode = repeatStateOf(it.repeatMode).next().toRepeatMode() }
    }

    fun changeShuffle() {
        whenConnected { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    /** Not part of [state], which would otherwise change every frame. 0 while there is nothing to read. */
    fun currentPosition(): Long {
        val controller = this.controller ?: return 0

        if (controller.mediaItemCount == 0) return 0

        return controller.currentPosition
    }

    /**
     * A future that completes after release() belongs to a connection nobody wants any more. Its
     * controller is released by releaseFuture() and must not be adopted.
     *
     * get() is not caught: the service is the app's own, and a connection it refuses is a bug.
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
     * onEvents() fires for loading and buffering too. SongModel has no equality of its own, so the
     * song is compared by id; without this every one of those events would reach the screens as a
     * new state and re-run the player screen's favourite queries.
     */
    private fun isSame(a: PlaybackState, b: PlaybackState): Boolean {
        return a.copy(currentSong = null) == b.copy(currentSong = null) && a.currentSong?.id == b.currentSong?.id
    }

    /** For the view models and tests that are not built by Hilt. */
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
