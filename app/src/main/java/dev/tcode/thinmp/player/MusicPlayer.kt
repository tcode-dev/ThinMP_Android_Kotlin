package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

class MusicPlayer(context: Context) {
    private lateinit var musicService: MusicService
    private lateinit var connection: ServiceConnection
    var bound: Boolean = false

    init {
        bindService(context)
    }

    fun start(songs: List<SongModel>, index: Int) {
        musicService.start(songs, index)
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
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false;
            }
        }
    }
}
