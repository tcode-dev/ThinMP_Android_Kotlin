package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

class MusicService : Service() {
    private val binder = MusicBinder()
    private var mediaPlayer : MediaPlayer? = null
    private var songs: List<SongModel> = emptyList()

    fun start(songs: List<SongModel>, index: Int) {
        this.songs = songs

        destroy()

        mediaPlayer = MediaPlayer.create(baseContext, this.songs[index].getMediaUri())

        mediaPlayer?.start()
    }

    private fun destroy() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }

        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
