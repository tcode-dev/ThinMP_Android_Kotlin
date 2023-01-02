package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener: MusicServiceListener {
    fun onBind() {}
}

class MusicPlayer(context: Context, val listener: MusicPlayerListener? = null) {
    private var musicService: MusicService? = null
    private lateinit var connection: ServiceConnection

    var bound: Boolean = false

    init {
        bindService(context)
    }

    fun start(songs: List<SongModel>, index: Int) {
        musicService?.start(songs, index)
    }

    fun play() {
        musicService?.play()
    }

    fun pause() {
        musicService?.pause()
    }

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() ?: false
    }

    fun getCurrentSong(): SongModel? {
        return musicService?.song
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
                if (listener != null) {
                    musicService!!.setListener(listener as MusicServiceListener)
                }
                listener?.onBind()
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false;
            }
        }
    }
}
