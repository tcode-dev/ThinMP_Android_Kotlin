package dev.tcode.thinmp.view.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicService

interface PlayInterface {
    var musicService: MusicService
    var connection: ServiceConnection
    var bound: Boolean

    fun bindService(context: Context) {
        connection = createConnection()
        context.bindService(
            Intent(context, MusicService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun start(song: SongModel) {
        musicService.start(song)
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
