package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.SongModel

class MusicService : Service() {
    var binder: IBinder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    fun initStart(song: SongModel) {
        mediaPlayer = MediaPlayer.create(baseContext, song.getUri())
        mediaPlayer?.start()
    }

    /**
     * bind
     */
    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }
}