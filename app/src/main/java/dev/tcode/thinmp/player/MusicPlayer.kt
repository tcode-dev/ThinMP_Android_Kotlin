package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener : MusicServiceListener {
    fun onBind() {}

    /** The service went away while bound. Not called for an unbind of our own. */
    fun onDisconnect() {}
}

class MusicPlayer(var listener: MusicPlayerListener) {
    private var musicService: MusicService? = null
    private var connection: ServiceConnection? = null
    private var isCreatingService = false

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() == true
    }

    /**
     * A screen only ever binds to a service that is already running; start() is what creates it.
     * When it is not running, bindService() waits, and connects only if something starts it.
     */
    fun start(context: Context, songs: List<SongModel>, index: Int) {
        val musicService = this.musicService

        if (musicService != null) {
            musicService.start(songs, index)

            return
        }

        if (isCreatingService) return

        // A binding still waiting for the service would never connect, since nothing else is going
        // to start it, so it is replaced by one that creates it.
        unbindService(context)
        isCreatingService = true
        bind(context, Context.BIND_AUTO_CREATE) { it.start(songs, index) }
    }

    fun play() {
        musicService?.play()
    }

    fun pause() {
        musicService?.pause()
    }

    fun prev() {
        musicService?.prev()
    }

    fun next() {
        musicService?.next()
    }

    fun seekTo(ms: Long) {
        musicService?.seekTo(ms)
    }

    fun getRepeat(): RepeatState {
        return musicService?.getRepeat() ?: RepeatState.OFF
    }

    fun changeRepeat() {
        musicService?.changeRepeat()
    }

    fun getShuffle(): Boolean {
        return musicService?.getShuffle() ?: false
    }

    fun changeShuffle() {
        musicService?.changeShuffle()
    }

    fun getCurrentSong(): SongModel? {
        return musicService?.getCurrentSong()
    }

    fun getCurrentPosition(): Long {
        return musicService?.getCurrentPosition() ?: 0
    }

    fun destroy(context: Context) {
        musicService?.removeEventListener(listener)

        unbindService(context)
    }

    /**
     * Binds without BIND_AUTO_CREATE, which is how the running service is found without asking
     * whether it runs: the connection arrives straight away when it does, and otherwise once it is
     * started. A screen binding this way never brings up a service of its own, which used to leave
     * a fresh MusicService that start() had never run on behind a mini player still on screen.
     */
    fun bindService(context: Context) {
        if (connection != null) return

        bind(context, 0) {}
    }

    private fun bind(context: Context, flags: Int, callback: (MusicService) -> Unit) {
        val connection = createConnection(context, callback)

        this.connection = connection
        context.bindService(MusicService.bindIntent(context), connection, flags)
    }

    /**
     * Also unbinds while a bind is still in flight. Guarding on a completed connection alone leaked
     * the ServiceConnection whenever a screen was left between bindService() and
     * onServiceConnected(), and let the connection go on to register a listener for a view model
     * that had already stopped.
     */
    private fun unbindService(context: Context) {
        val connection = this.connection ?: return

        context.unbindService(connection)
        this.connection = null
        musicService = null
        isCreatingService = false
    }

    private fun createConnection(context: Context, callback: (MusicService) -> Unit): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                val musicService = binder.getService()

                this@MusicPlayer.musicService = musicService
                musicService.addEventListener(listener)
                callback(musicService)
                listener.onBind()
                isCreatingService = false
            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicService = null
                listener.onDisconnect()
            }

            /**
             * A binding without BIND_AUTO_CREATE dies with the service it was connected to and
             * never connects again, so it is dropped. The next bindService() or start() makes a
             * new one.
             */
            override fun onBindingDied(name: ComponentName) {
                unbindService(context)
            }
        }
    }
}
