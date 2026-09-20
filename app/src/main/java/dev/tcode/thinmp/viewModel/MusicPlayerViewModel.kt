package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener

/**
 * Owns a [MusicPlayer] and binds it to [dev.tcode.thinmp.player.MusicService] for as long as the
 * screen is in the foreground: bound on construction and on every return to the screen, unbound
 * on ON_STOP. Whether the service is running decides whether there is anything to bind to.
 *
 * The first ON_RESUME follows straight after init, which has already bound, so it is skipped.
 */
abstract class MusicPlayerViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    protected val musicPlayer: MusicPlayer = MusicPlayer(this)
    private var initialized: Boolean = false

    init {
        bindService()
    }

    override fun onResume() {
        if (!initialized) {
            initialized = true

            return
        }

        onReturn()
        bindService()
    }

    override fun onStop() {
        musicPlayer.destroy(getApplication())
    }

    /**
     * ON_STOP is the normal way out, but it is delivered by the screen's lifecycle observer, and a
     * view model cleared without one would keep the service bound and stay registered as its
     * listener for good. destroy() does nothing when there is no binding, so the two overlap safely.
     */
    override fun onCleared() {
        musicPlayer.destroy(getApplication())
    }

    /** Runs on every ON_RESUME but the first, before the service is bound again. */
    protected open fun onReturn() {}

    private fun bindService() {
        if (musicPlayer.isServiceRunning()) {
            musicPlayer.bindService(getApplication())
        }
    }
}
