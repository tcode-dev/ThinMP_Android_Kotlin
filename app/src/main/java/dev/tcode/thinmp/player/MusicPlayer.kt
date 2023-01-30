package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.config.ShuffleState
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener: MusicServiceListener {
    fun onBind() {}
}

class MusicPlayer(context: Context) {
    private var musicService: MusicService? = null
    private lateinit var connection: ServiceConnection
    private var listener: MusicPlayerListener? = null

    var bound: Boolean = false

    init {
        bindService(context)
    }

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

    fun seekTo(msec: Int) {
        musicService?.seekTo(msec)
    }

    fun getRepeat(): RepeatState {
        return musicService?.getRepeat() ?: RepeatState.OFF
    }

    fun setRepeat() {
        musicService?.setRepeat()
    }

    fun getShuffle(): ShuffleState {
        return musicService?.getShuffle() ?: ShuffleState.OFF
    }

    fun setShuffle() {
        musicService?.setShuffle()
    }

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() ?: false
    }

    fun getCurrentSong(): SongModel? {
        return musicService?.song
    }

    fun addEventListener(listener: MusicPlayerListener) {
        this.listener = listener
        musicService?.addEventListener(listener)
    }

    fun removeEventListener() {
        this.listener = null
        musicService?.removeEventListener()
    }

    fun unbindService(context: Context) {
        context.unbindService(connection)
    }

    fun getCurrentPosition(): Int {
        return musicService?.getCurrentPosition() ?: 0
    }

    private fun bindService(context: Context) {
        connection = createConnection()
        context.bindService(
            Intent(context, MusicService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
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

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false;
            }
        }
    }
}