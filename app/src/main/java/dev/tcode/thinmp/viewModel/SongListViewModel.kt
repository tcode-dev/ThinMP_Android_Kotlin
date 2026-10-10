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
 * A screen that lists songs and starts playback from one of them. The list is reloaded on every
 * return to the screen and whenever the service drops a song it could not play, and the mini
 * player is shown while the queue has a song.
 *
 * [isVisiblePlayer] starts from PlaybackController's current state, so a screen opened for the
 * first time shows the mini player from its first frame rather than once a bind completes.
 *
 * The first ON_RESUME follows straight after init, which has already loaded, so it is skipped.
 */
abstract class SongListViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private val playbackController = PlaybackController.from(application)
    private var initialized: Boolean = false

    val isVisiblePlayer: StateFlow<Boolean> =
        playbackController.state.map { it.hasQueue }.stateIn(viewModelScope, SharingStarted.Eagerly, playbackController.state.value.hasQueue)

    /** The list [start] plays from. */
    protected abstract val songs: List<SongModel>

    protected abstract fun load()

    init {
        // Nothing is emitted before init returns, so load() never runs ahead of the subclass's own
        // fields.
        viewModelScope.launch {
            playbackController.songRemoved.collect { load() }
        }
    }

    /**
     * Takes the id rather than the row's position. The list is reloaded on every return to the
     * screen and after a playback error, so a position taken when the row was composed can name a
     * different song, or none at all, by the time the tap arrives. A song the list no longer holds
     * is ignored.
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

/** Where [songId] sits in [songs], or null once the list no longer holds it. */
fun indexOfSong(songs: List<SongModel>, songId: SongId): Int? {
    val index = songs.indexOfFirst { it.songId == songId }

    return if (index < 0) null else index
}
