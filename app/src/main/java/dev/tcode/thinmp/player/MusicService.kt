package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

class MusicService : Service() {
    private val binder = MusicBinder()

    fun start(song: SongModel) {
        val mediaPlayer = MediaPlayer.create(baseContext, song.getUri())

        mediaPlayer?.start()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
