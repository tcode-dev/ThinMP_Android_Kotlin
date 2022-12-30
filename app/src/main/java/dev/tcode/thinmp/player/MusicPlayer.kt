package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener {
    fun onBind() {}
}

class MusicPlayer(context: Context) {
    private lateinit var musicService: MusicService
    private lateinit var connection: ServiceConnection
    private var listener: MusicPlayerListener? = null

    var bound: Boolean = false

    init {
        bindService(context)
    }

    fun setListener(listener: MusicPlayerListener) {
        this.listener = listener
    }

    fun start(songs: List<SongModel>, index: Int) {
        musicService.start(songs, index)
    }

    fun getCurrentSong(): SongModel? {
        return if (bound) {
            musicService.song
        } else {
            null
        }
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
                bound = true;
                listener?.onBind()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false;
            }
        }
    }
}
