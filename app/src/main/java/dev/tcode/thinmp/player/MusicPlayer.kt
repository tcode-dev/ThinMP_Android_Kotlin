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

class MusicPlayer(context: Context) {
    private var musicService: MusicService? = null
    private lateinit var connection: ServiceConnection
    private var listener: MusicPlayerListener? = null
    private var bound = false

    fun start(songs: List<SongModel>, index: Int) {
        if (songs.isEmpty()) {
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

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() ?: false
    }

    fun getCurrentSong(): SongModel? {
        return musicService?.getCurrentSong()
    }

    fun addEventListener(listener: MusicPlayerListener) {
        this.listener = listener
        musicService?.addEventListener(listener)
    }

    fun getCurrentPosition(): Long {
        return musicService?.getCurrentPosition() ?: 0
    }

    fun destroy(context: Context) {
        removeEventListener()
        unbindService(context)
    }

    fun removeEventListener() {
        this.listener = null
        musicService?.removeEventListener()
    }

    fun bindService(context: Context) {
        if (bound) return

        connection = createConnection()
        context.bindService(
            Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindService(context: Context) {
        if (bound) {
            context.unbindService(connection)
            musicService = null
            bound = false
        }
    }

    private fun createConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.getService()
                listener?.let { musicService!!.addEventListener(it) }
                listener?.onBind()
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
    }
}