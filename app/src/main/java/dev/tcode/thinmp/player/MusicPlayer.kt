package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener : MusicServiceListener {
    fun onBind() {}
}

class MusicPlayer(var listener: MusicPlayerListener) {
    private var musicService: MusicService? = null
    private lateinit var connection: ServiceConnection
    private var isConnecting = false
    private var bound = false

    fun isServiceRunning(): Boolean {
        return MusicService.isServiceRunning
    }

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() == true
    }

    fun start(context: Context, songs: List<SongModel>, index: Int) {
        if (isConnecting) return
        if (isPreparing()) return

        if (!isServiceRunning()) {
            context.startForegroundService(Intent(context, MusicService::class.java))
            bindService(context) { musicService?.start(songs, index) }

            return
        }

        if (!bound) {
            bindService(context)

            return
        }

        musicService?.start(songs, index)
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

    fun bindService(context: Context, callback: () -> Unit? = {}) {
        if (isConnecting || bound) return

        isConnecting = true
        connection = createConnection(callback)
        context.bindService(
            Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE
        )
    }

    private fun isPreparing(): Boolean {
        return musicService?.isPreparing() == true
    }

    private fun unbindService(context: Context) {
        if (!bound) return

        context.unbindService(connection)
        musicService = null
        bound = false
    }

    private fun createConnection(callback: () -> Unit? = {}): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.getService()
                musicService!!.addEventListener(listener)
                callback()
                listener.onBind()
                isConnecting = false
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
    }
}