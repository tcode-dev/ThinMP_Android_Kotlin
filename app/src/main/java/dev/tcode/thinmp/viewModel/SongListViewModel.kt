package dev.tcode.thinmp.viewModel

import android.app.Application
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A screen that lists songs and starts playback from one of them. The list is reloaded on every
 * return to the screen and whenever the service drops a song it could not play, and the mini
 * player is shown once the service is running.
 */
abstract class SongListViewModel(application: Application) : MusicPlayerViewModel(application) {
    private val _isVisiblePlayer = MutableStateFlow(musicPlayer.isServiceRunning())
    val isVisiblePlayer: StateFlow<Boolean> = _isVisiblePlayer.asStateFlow()

    /** The list [start] plays from. */
    protected abstract val songs: List<SongModel>

    protected abstract fun load()

    /**
     * Takes the id rather than the row's position. The list is reloaded on every return to the
     * screen and after a playback error, so a position taken when the row was composed can name a
     * different song, or none at all, by the time the tap arrives. A song the list no longer holds
     * is ignored.
     */
    fun start(songId: SongId) {
        val songs = this.songs
        val index = indexOfSong(songs, songId) ?: return

        musicPlayer.start(getApplication(), songs, index)
    }

    override fun onReturn() {
        load()
        updateIsVisiblePlayer()
    }

    override fun onBind() {
        updateIsVisiblePlayer()
    }

    override fun onError() {
        load()
    }

    private fun updateIsVisiblePlayer() {
        _isVisiblePlayer.value = musicPlayer.isServiceRunning()
    }
}

/** Where [songId] sits in [songs], or null once the list no longer holds it. */
fun indexOfSong(songs: List<SongModel>, songId: SongId): Int? {
    val index = songs.indexOfFirst { it.songId == songId }

    return if (index < 0) null else index
}
