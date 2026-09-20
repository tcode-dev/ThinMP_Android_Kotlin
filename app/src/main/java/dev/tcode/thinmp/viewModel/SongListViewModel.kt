package dev.tcode.thinmp.viewModel

import android.app.Application
import dev.tcode.thinmp.model.media.SongModel
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

    fun start(index: Int) {
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
